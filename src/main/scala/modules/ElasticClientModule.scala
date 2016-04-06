/**
  * Created by gqadonis on 3/22/16.
  */
package modules

import javax.inject.Singleton

import com.google.inject.Provides
import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri}
import com.twitter.inject.TwitterModule
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.client.transport.TransportClient
//import org.elasticsearch.common.settings.ImmutableSettings

object ElasticClientModule extends TwitterModule {
  private val host = flag("host", "localhost", "host name of ES")
  private val port = flag("port", 9300, "port no of ES")
  val timeout = flag("timeout", 30, "default timeout duration of execution")

  @Singleton
  @Provides
  def provideElasticClient(): ElasticClient = {
    println("------------------elastic client init-------------------")
    //ElasticClient.remote(host(), port())

    val uri = ElasticsearchClientUri("elasticsearch://127.0.0.1:9300")
    val settings = Settings.settingsBuilder.put("cluster.name", "elasticsearch_gqadonis").build()


    //val uri = ElasticsearchClientUri("elasticsearch://127.0.0.1:9300")
    //val settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch_gqadonis").build()
    //val settings = ImmutableSettings.settingsBuilder()
      //.put("http.enabled", true).put("cluster.name", "elasticsearch_gqadonis").build()
    //ElasticClient.remote(settings,(uri))
    ElasticClient.transport(settings, uri)
  }
}
