package be.cytomine

import be.cytomine.project.Project
import be.cytomine.security.SecUser
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclEntry
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclObjectIdentity
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclSid

import static org.springframework.security.acls.domain.BasePermission.*

import be.cytomine.Exception.ForbiddenException
import be.cytomine.security.User
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ontology.Ontology
import be.cytomine.processing.Software

/**
 * CytomineDomain is the parent class for all domain.
 * It allow to give an id to each instance of a domain, to get a created date,...
 */
abstract class CytomineDomain  implements Comparable{

    def springSecurityService
    def cytomineService
    def sequenceService
    def securityService

    static def grailsApplication
    Long id
    Date created
    Date updated

    static mapping = {
        tablePerHierarchy false
        id generator: "assigned"
    }

    static constraints = {
        created nullable: true
        updated nullable: true
    }

    public beforeInsert() {
        if (!created) {
            created = new Date()
        }
        if (id == null) {
            id = sequenceService.generateID()
        }
    }

  def beforeValidate() {
      if (!created) {
          created = new Date()
      }
      if (id == null) {
          id = sequenceService.generateID()
      }
  }

    public beforeUpdate() {
        updated = new Date()
    }

    /**
     * This function check if a domain already exist (e.g. project with same name).
     * A domain that must be unique should rewrite it and throw AlreadyExistException
     */
    void checkAlreadyExist() {
        //do nothing ; if override by a sub-class, should throw AlreadyExist exception
    }

    /**
     * Return domain project (annotation project, image project...)
     * By default, a domain has no project.
     * You need to override projectDomain() in domain class
     * @return Domain project
     */
    public Project projectDomain() {
        return null;
    }

    /**
     * Return domain user (annotation user, image user...)
     * By default, a domain has no user.
     * You need to override userDomain() in domain class
     * @return Domain user
     */
    public SecUser userDomain() {
        return null;
    }

    /**
     * Return domain ontology (term ontology, relation-term ontology...)
     * By default, a domain has no ontology linked.
     * You need to override ontologyDomain() in domain class
     * @return Domain ontology
     */
    public Ontology ontologyDomain() {
        return null;
    }

    /**
     * Return domain software (parameter software, job software...)
     * By default, a domain has no software linked.
     * You need to override softwareDomain() in domain class
     * @return Domain software
     */
    public Software softwareDomain() {
        return null;
    }

    /**
     * Build callback data for a domain (by default null)
     * Callback are metadata used by client
     * You need to override getCallBack() in domain class
     * @return Callback data
     */
    def getCallBack() {
        return null
    }

    /**
     * This method check if current user has permission on the current domain
     * @param permission Type of permission (read, admin,...)
     * @return True if user has this permission on the current domain, otherwise false
     */
    boolean hasPermission(String permission) {
        try {
            return hasPermission(this,permission)
        } catch (Exception e) {e.printStackTrace()}
        return false
    }

    boolean checkReadPermission() {
        println "checkReadPermission1=${hasPermission("READ")}"
        println "checkReadPermission2=${cytomineService.currentUser.admin}"
        return hasPermission("READ") || cytomineService.currentUser.admin
    }

    boolean checkWritePermission() {
        return hasPermission("WRITE") || cytomineService.currentUser.admin
    }

    boolean checkDeletePermission() {
        println "checkReadPermission1=${hasPermission("DELETE")}"
        println "checkReadPermission2=${cytomineService.currentUser.admin}"
        return hasPermission("DELETE") || cytomineService.currentUser.admin
    }

    /**
     * This method check if current user has permission on the domain from className with this id
     * @param id  Domain id
     * @param className Domain class
     * @param permission Type of permission
     * @return  True if user has this permission on the specific domain, otherwise false
     */
    boolean hasPermission(Long id,String className, String permission) {
        try {
            def obj = grailsApplication.classLoader.loadClass(className).get(id)
            return hasPermission(obj,permission)
        } catch (Exception e) {
            log.error e.toString()
            e.printStackTrace()}
        return false
    }

    /**
     *  This method check if current user has permission on a domain
     * @param domain Domainto check
     * @param permissionStr Type of permission
     * @return  True if user has this permission on the specific domain, otherwise false
     */
    boolean hasPermission(def domain,String permissionStr) {
        try {
            SecUser currentUser = cytomineService.getCurrentUser()
            String usernameParentUser = currentUser.humanUsername()
            int permission = -1
            if(permissionStr.equals("READ")) permission = READ.mask
            else if(permissionStr.equals("WRITE")) permission = WRITE.mask
            else if(permissionStr.equals("DELETE")) permission = DELETE.mask
            else if(permissionStr.equals("CREATE")) permission = CREATE.mask
            else if(permissionStr.equals("ADMIN")) permission = ADMINISTRATION.mask

            //TODO:: this function is call very often, make a direct SQL request instead 3 request

            AclObjectIdentity aclObject = AclObjectIdentity.findByObjectId(domain.id)
            AclSid aclSid = AclSid.findBySid(usernameParentUser)
            if(!aclObject) return false
            if(!aclSid) return false

            boolean hasPermission = false;
            List<AclEntry> acls = AclEntry.findAllByAclObjectIdentityAndSid(aclObject,aclSid)
            acls.each { acl ->
                if(acl.mask>=permission) {
                    hasPermission=true
                }
            }
            return hasPermission

        } catch (Exception e) {
            log.error e.toString()
            e.printStackTrace()
        }
        return false
    }

    /**
     * Check if current user is the object creator
     */
    boolean isCurrentUserCreator() {
        SecUser currentUser = cytomineService.getCurrentUser()
        def creator = retrieveCreator()
        return creator && creator.id==currentUser.id
    }

    User retrieveCreator() {
        List<User> users = SecUser.executeQuery("select secUser from AclObjectIdentity as aclObjectId, AclSid as aclSid, SecUser as secUser where aclObjectId.objectId = "+this.id+" and aclObjectId.owner = aclSid.id and aclSid.sid = secUser.username and secUser.class = 'be.cytomine.security.User'")
        User user = users.isEmpty() ? null : users.first()
        return user
    }

    int compareTo(obj) {
        created.compareTo(obj.created)
    }
}
