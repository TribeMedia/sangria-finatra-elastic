package controllers

import javax.inject.Singleton

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

/**
  * Created by gqadonis on 3/22/16.
  */
@Singleton
class AssetController extends Controller {
  get("/assets/:type/:file") { request: Request =>
    val file = request.getParam("file")
    val assetType = request.getParam("type")
    response.ok.file(s"$assetType/$file")
  }
}
