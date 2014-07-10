package be.cytomine.utils

import be.cytomine.api.RestController

//@RestApiObject(name = "city")
class ArchiveController extends RestController {

    def grailsApplication
    def modelService
    def springSecurityService
    def archiveCommandService


    def archive() {
        archiveCommandService.archiveOldCommand()
        responseSuccess([])
    }
}