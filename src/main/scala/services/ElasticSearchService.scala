package services

/**
  * Created by gqadonis on 3/22/16.
  */

import javax.inject.{Inject, Singleton}

import models.{Quote, QuoteCreationModel}
import com.sksamuel.elastic4s.ElasticDsl.{delete => delete4s, _}
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._
import com.twitter.finatra.annotations.Flag
import org.elasticsearch.action.update.UpdateResponse
import org.elasticsearch.index.get.GetField
import com.twitter.finagle.http
import com.twitter.finagle.{Http, Service}
import org.jboss.netty.handler.codec.http.{DefaultHttpRequest, HttpMethod, HttpVersion}
import com.twitter.util.{Await, Future}
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}
import com.twitter.io.Charsets
import graphQL.SchemaDefinition
import org.json4s.JsonAST.JObject
import org.json4s.native.JsonMethods
import resolvers.QueryResolver

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

@Singleton
class ElasticSearchService @Inject()
(elasticClient: ElasticClient, @Flag("timeout") timeout: Short) extends QueryResolver {
  private val _index: String = "quotes"
  private val _type: String = "quote"

  def getItem(id: String) = {
    val req = get id id from _index / _type
    val future = this.elasticClient.execute(req)
    val resp = scala.concurrent.Await.result(future, timeout.seconds)
    parseToItem(id, Some(resp.getFields))
  }

  def getMatchingItems(keyword: Option[String]) = {
    val req = search in _index / _type query s"*${keyword.getOrElse('*')}*" size 999
    val future = this.elasticClient.execute(req)
    scala.concurrent.Await.result(future.map(res => res.as[Quote]), timeout.seconds).toList
  }

  def addItem(quote: QuoteCreationModel): Quote = {
    val future = this.elasticClient.execute(index into _index / _type source quote)
    val resp = scala.concurrent.Await.result(future, timeout.seconds)
    refreshIndex
    Quote(resp.getId, quote.a2z, quote.lastname, quote.author, quote.profession, quote.nationality, quote.birthdate,
      quote.deathdate, quote.quote, quote.keywords)
  }

  def updateItem(quote: Quote): Option[Quote] = {
    val req = update id quote._id in _index / _type source quote includeSource
    //    val future = this.client.execute(req).map(rlt => rlt.getGetResult.sourceAsString)
    val resp: UpdateResponse = scala.concurrent.Await.result(this.elasticClient.execute(req), timeout.seconds)
    val map = resp.getGetResult.getFields()
    refreshIndex
    parseToItem(resp.getId(), Some(map))
  }

  def deleteItem(id: String) = {
    val req = delete4s id id from _index -> _type
    val future = this.elasticClient.execute(req) map (res => (res.getId, res.getIndex))
    scala.concurrent.Await.result(future, timeout.seconds)
    refreshIndex
    id
  }

  /*
  "a2z" : { "type" : "string" },
        "lastname" : { "type" : "string" },
        "author" : { "type" : "string" },
		"profession" : { "type" : "string" },
		"nationality" : { "type" : "string" },
		"birthdate" : { "type" : "date", "format":"dd-MMM-yyyy" },
		"deathdate" : { "type" : "date", "format":"dd-MMM-yyyy"  },
		"quote" : { "type" : "string" },
		"keywords" : { "type" : "string" }

   */
  private def parseToItem(id: String, map: Option[java.util.Map[String, GetField]]): Option[Quote] = {
    map match {
      case Some(null) => None
      case Some(map) => Some(Quote(id, map.get("a2z").toString(), map.get("lastname").toString(), map.get("author").toString(),
        map.get("profession").toString(), map.get("nationality").toString(), map.get("birthdate").toString(), map.get("deathdate").toString(),
        map.get("quote").toString(), map.get("keywords").toString()))
    }
  }

  private def isIndexExists(): Boolean = {
    val req = indexExists(this._index)
    scala.concurrent.Await.result(this.elasticClient.execute(req), timeout.seconds).isExists
  }

  //refresh data manually
  def refreshIndex() = {
    scala.concurrent.Await.result(this.elasticClient.execute(refresh index this._index), timeout.seconds)
  }

  def checkAndInitIndex() = {
    //loadIndexMappings()
  }

  /* {
        "quotes":
        {
          "mappings":
          {
            "quote":
            {
              "properties":
              {
                "a2z":{"type":"string"},
                "lastname":{"type":"string"},
                "author":{"type":"string"},
                "birthdate":{"type":"date","format":"dd-MMM-yyyy"},
                "deathdate":{"type":"date","format":"dd-MMM-yyyy"},
                "keywords":{"type":"string"},
                "nationality":{"type":"string"},
                "profession":{"type":"string"},
                "quote":{"type":"string"}
              }
             }
            }
           }
          }
      */
  def loadIndexMappings() = {
    // load customer mapping into the newly created index on server...
    val client: Service[http.Request, http.Response] = Http.newService("localhost:9200")
    val request = http.Request(http.Method.Get, "/quotes/_mapping/quote")
    val response: Future[http.Response] = client(request)

    response onSuccess { resp: http.Response =>
      val contentJson = resp.asInstanceOf[HttpResponse].getContent.toString(Charsets.Utf8)
      val json = JsonMethods.parse(contentJson).asInstanceOf[JObject]
      //OpenSchemaDefinition.build(json, this)
    }

    com.twitter.util.Await.ready(response)
  }
}
