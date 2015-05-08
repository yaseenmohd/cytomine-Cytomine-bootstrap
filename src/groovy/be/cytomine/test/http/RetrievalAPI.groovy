package be.cytomine.test.http

import be.cytomine.test.Infos

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Discipline to Cytomine with HTTP request during functional test
 */
class RetrievalAPI extends DomainAPI {

    static def getResults(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/$id/retrieval.json"
        return doGET(URL, username, password)
    }
}