package services

/**
  * Created by gqadonis on 3/22/16.
  */

import javax.inject.{Inject, Singleton}

import models.{Customer, CustomerCreationModel}
import com.sksamuel.elastic4s.ElasticDsl.{delete => delete4s, _}
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._
import com.twitter.finatra.annotations.Flag
import org.elasticsearch.action.update.UpdateResponse

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

@Singleton
class ElasticSearchService @Inject()
(elasticClient: ElasticClient, @Flag("timeout") timeout: Short){
  private val _index: String = "bank"
  private val _type: String = "customer"

  def getCustomer(id: String) = {
    val req = get id id from _index / _type
    val future = this.elasticClient.execute(req)
    val resp = Await.result(future, timeout.seconds)
    val map = resp.getSourceAsMap
    parseToCustomer(resp.getId, Some(map))
  }

  def getMatchingCustomers(keyword: Option[String]) = {
    val req = search in _index / _type query s"*${keyword.getOrElse('*')}*" size 999
    val future = this.elasticClient.execute(req)
    Await.result(future.map(res => res.as[Customer]), timeout.seconds).toList
  }

  def addCustomer(customer: CustomerCreationModel): Customer = {
    val future = this.elasticClient.execute(index into _index / _type source customer)
    val resp = Await.result(future, timeout.seconds)
    refreshIndex
    Customer(resp.getId, customer.lastName, customer.firstName, customer.ssn)
  }

  def updateCustomer(customer: Customer): Option[Customer] = {
    val req = update id customer._id in _index / _type source customer includeSource
    //    val future = this.client.execute(req).map(rlt => rlt.getGetResult.sourceAsString)
    val resp: UpdateResponse = Await.result(this.elasticClient.execute(req), timeout.seconds)
    val map = resp.getGetResult.sourceAsMap
    refreshIndex
    parseToCustomer(resp.getId, Some(map))
  }

  def deleteCustomer(id: String) = {
    val req = delete4s id id from _index -> _type
    val future = this.elasticClient.execute(req) map (res => (res.getId, res.getIndex))
    Await.result(future, timeout.seconds)
    refreshIndex
    id
  }

  private def parseToCustomer(id: String, map: Option[java.util.Map[String, AnyRef]]): Option[Customer] = {
    map match {
      case Some(null) => None
      case Some(map) => Some(Customer(id, map.get("lastName").toString, map.get("firstName").toString, map.get("ssn").toString))
    }
  }

  private def isIndexExists(): Boolean = {
    val req = indexExists(this._index)
    Await.result(this.elasticClient.execute(req), timeout.seconds).isExists
  }

  //refresh data manually
  def refreshIndex() = {
    Await.result(this.elasticClient.execute(refresh index this._index), timeout.seconds)
  }

  def checkAndInitIndex() = {
    if (!this.isIndexExists) {
      this.elasticClient.execute(create index this._index)
    }
  }
}
