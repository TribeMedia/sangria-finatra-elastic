import javax.inject.Inject

import com.twitter.finatra.http.routing.HttpWarmup
import services.ElasticSearchService
import com.twitter.finatra.utils.Handler

/**
  * Created by gqadonis on 3/22/16.
  */
class WarmupHandler @Inject()(httpWarmup: HttpWarmup, searchService: ElasticSearchService) extends Handler {
  override def handle() = {
    searchService.checkAndInitIndex
  }
}
