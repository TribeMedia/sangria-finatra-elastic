import controllers.{AssetController, SearchController}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.CommonFilters
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.logging.filter.{LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.logging.modules.Slf4jBridgeModule
import modules.ElasticClientModule

/**
  * Created by gqadonis on 3/22/16.
  */

object SearchServerMain extends SearchServer

class SearchServer extends HttpServer{
  override def modules = Seq(Slf4jBridgeModule, ElasticClientModule)

  override def configureHttp(router: HttpRouter) {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .add[SearchController]
      .add[AssetController]
  }

  override def warmup() {
    run[WarmupHandler]()
  }
}
