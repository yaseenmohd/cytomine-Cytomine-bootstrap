package be.cytomine.security

import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.test.Infos
import be.cytomine.test.http.JobAPI
import be.cytomine.test.http.ProjectAPI
import be.cytomine.utils.BasicInstance
import grails.converters.JSON
import be.cytomine.processing.JobParameter
import be.cytomine.test.http.JobParameterAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class JobParameterSecurityTests extends SecurityTestsAbstract{


  void testJobParameterSecurityForCytomineAdmin() {

      //Get user1
      User user1 = getUser1()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assertEquals(200, result.code)
      Project project = result.data

      //Add jobParameter instance to project
      JobParameter jobParameter = BasicInstance.getBasicJobParameterNotExist()
      jobParameter.job.project = project

      //check if admin user can access/update/delete
      result = JobParameterAPI.create(jobParameter.encodeAsJSON(),SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN)
      assertEquals(200, result.code)
      jobParameter = result.data
      assertEquals(200, JobParameterAPI.show(jobParameter.id,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)
      result = JobParameterAPI.listByJob(jobParameter.job.id,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN)
      assertEquals(200, result.code)
      assertTrue(JobParameterAPI.containsInJSONList(jobParameter.id,JSON.parse(result.data)))
      assertEquals(200, JobParameterAPI.update(jobParameter.id,jobParameter.encodeAsJSON(),SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)
      assertEquals(200, JobParameterAPI.delete(jobParameter.id,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)
  }

  void testJobParameterSecurityForProjectUser() {

      //Get user1
      User user1 = getUser1()
      User user2 = getUser2()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assertEquals(200, result.code)
      Project project = result.data

      //add right to user 2
      def resAddUser = ProjectAPI.addUserProject(project.id,user2.id,SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      Infos.printRight(project)
      assertEquals(200, resAddUser.code)

      //Add jobParameter instance to project
      JobParameter jobParameter = BasicInstance.getBasicJobParameterNotExist()
      jobParameter.job.project = project

      //check if user 2 can access/update/delete
      result = JobParameterAPI.create(jobParameter.encodeAsJSON(),SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2)
      assertEquals(200, result.code)
      jobParameter = result.data
      assertEquals(200, JobParameterAPI.show(jobParameter.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
      result = JobParameterAPI.listByJob(jobParameter.job.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2)
      assertEquals(200, result.code)
      assertTrue(JobParameterAPI.containsInJSONList(jobParameter.id,JSON.parse(result.data)))
      //assertEquals(200, JobParameterAPI.update(jobParameter,USERNAME2,PASSWORD2).code)
      assertEquals(200, JobParameterAPI.delete(jobParameter.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
  }

  void testJobParameterSecurityForSimpleUser() {

      //Get user1
      User user1 = getUser1()
      User user2 = getUser2()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assertEquals(200, result.code)
      Project project = result.data

      //Add jobParameter instance to project
      JobParameter jobParameter = BasicInstance.getBasicJobParameterNotExist()
      jobParameter.job.project = project

      //check if simple user can access/update/delete
      result = JobParameterAPI.create(jobParameter.encodeAsJSON(),SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2)
      assertEquals(403, result.code)
      jobParameter = result.data

      jobParameter = BasicInstance.createOrGetBasicJobParameter()
      jobParameter.project = project
      jobParameter.save(flush:true)

      assertEquals(403, JobParameterAPI.show(jobParameter.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
      assertEquals(403,JobParameterAPI.listByJob(jobParameter.job.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
      //assertEquals(403, JobParameterAPI.update(jobParameter,USERNAME2,PASSWORD2).code)
      assertEquals(403, JobParameterAPI.delete(jobParameter.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)
  }

}