package be.cytomine

import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ImageFilterAPI
import be.cytomine.test.http.ImageFilterProjectAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class ImageFilterTests  {

  void testListImageFilterWithCredential() {
      def result = ImageFilterAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
  }

  void testShowImageFilterWithCredential() {
      def result = ImageFilterAPI.show(BasicInstanceBuilder.getImageFilter().id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject

      result = ImageFilterAPI.show(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }

  /*
    Image filter project
  */
    void testListImageFilterProject() {
        def result = ImageFilterProjectAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListImageFilterProjectByProject() {
        def project = BasicInstanceBuilder.getProject()
        def result = ImageFilterProjectAPI.listByProject(project.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListImageFilterProjectByProjectNotExist() {
        def result = ImageFilterProjectAPI.listByProject(-99,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testAddImageFilterProject() {
        def ifp = BasicInstanceBuilder.getImageFilterProjectNotExist()
        def result = ImageFilterProjectAPI.create(ifp.encodeAsJSON(),Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testDeleteImageFilterProject() {
       def ifp = BasicInstanceBuilder.getImageFilterProjectNotExist()
       ifp.save(flush: true)
        def result = ImageFilterProjectAPI.delete(ifp.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }



}