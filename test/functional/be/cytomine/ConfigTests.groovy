package be.cytomine

import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ConfigAPI
import be.cytomine.utils.Config
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by hoyoux on 06.05.15.
 */
class ConfigTests {

    //TEST SHOW

    void testConfigShow() {

        def config = BasicInstanceBuilder.getConfigNotExist()
        def result = ConfigAPI.create(config.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        String key =  result.data.key

        result = ConfigAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testConfigShowNotExist() {
        def result = ConfigAPI.show("-1", Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    //TEST LIST
    void testConfigList() {
        def result = ConfigAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    //TEST DELETE
    void testConfigDelete() {
        def configToDelete = BasicInstanceBuilder.getConfigNotExist(true)
//        assert configToDelete.save(flush: true) != null

        def key = configToDelete.key
        def id = configToDelete.id
        def result = ConfigAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        //UNDO & REDO
        result = ConfigAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

        result = ConfigAPI.undo()
        assert 200 == result.code

        result = ConfigAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = ConfigAPI.redo()
        assert 200 == result.code

        result = ConfigAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }
//
    //TEST ADD
    void testConfigAddCorrect() {
        def configToAdd = BasicInstanceBuilder.getConfigNotExist()

        def result = ConfigAPI.create(configToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        String key =  result.data.key

        //UNDO & REDO
        result = ConfigAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = ConfigAPI.undo()
        assert 200 == result.code

        result = ConfigAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

        result = ConfigAPI.redo()
        assert 200 == result.code

        result = ConfigAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testConfigAddAlreadyExist() {
        def configToAdd = BasicInstanceBuilder.getConfig()
        def result = ConfigAPI.create(configToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert (409 == result.code) || (400 == result.code)
    }

    //TEST UPDATE
    void testConfigUpdateCorrect() {
        Config configToUpdate = BasicInstanceBuilder.getConfig()

        def jsonConfig = configToUpdate.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonConfig)
        jsonUpdate.value = "test2"

        def result = ConfigAPI.update(configToUpdate.key, jsonUpdate.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject

        assert json.config.value== "test2"
    }
}
