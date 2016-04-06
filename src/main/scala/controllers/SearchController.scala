package controllers

import javax.inject.Inject

import com.google.inject.Singleton
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.request.RequestUtils
import graphQL.SchemaDefinition
import org.json4s.JObject
import org.json4s.JsonAST.JString
import org.json4s.native.{Json, JsonMethods}
import org.json4s.native.JsonMethods._
import sangria.execution.Executor
import sangria.parser.QueryParser
import services.ElasticSearchService
import views.IndexView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by gqadonis on 3/22/16.
  */

@Singleton
class SearchController@Inject()(searchService: ElasticSearchService)() extends Controller {
  val _awaitTimeout = 30.seconds

  val executor = Executor(
    schema = SchemaDefinition.QuoteSchema,
    userContext = searchService)

  get("/index") { request: Request =>
    IndexView()
  }

  get("/api/customer") { request: Request =>
    val reqJson = request.getParam("query")

    QueryParser.parse(reqJson) match {
      // query parsed successfully, time to execute it!
      case Success(queryAst) =>
        Await.result(executor.execute(queryAst), _awaitTimeout)

      // can't parse GraphQL query, return error
      case Failure(error) => error.getMessage
    }
  }

  post("/api/customers") { request: Request =>
    val reqJson = parse(request.getContentString)
    val JString(mutation) = reqJson \ "mutation"

    QueryParser.parse(mutation) match {
      // query parsed successfully, time to execute it!
      case Success(queryAst) =>
        Await.result(executor.execute(queryAst), _awaitTimeout)

      // can't parse GraphQL query, return error
      case Failure(error) => error.getMessage
    }
  }

  get("/graphql") { request: Request =>
    val reqJson = request.getParam("query")
    /*val variables = request.getParam("variables") match {
      case s:String => Some(parseVariables(s))
      case _ => None
    }*/
    val operation = request.getParam("operation") match {
      case s:String => Some(s)
      case _ => None
    }

    executeQuery(query = reqJson, variables = None, operation = operation)
  }

  post("/graphql") { request: Request =>
    val reqJson = parse(request.getContentString)
    val JString(mutation) = reqJson \ "mutation"

    QueryParser.parse(mutation) match {
      // query parsed successfully, time to execute it!
      case Success(queryAst) =>
        Await.result(executor.execute(queryAst), _awaitTimeout)

      // can't parse GraphQL query, return error
      case Failure(error) => error.getMessage
    }
  }

  private def parseVariables(variables: String) =
    if (variables.trim == "") JObject() else JsonMethods.parse(variables)

  private def executeQuery(query: String, variables: Option[JObject], operation: Option[String]) =
    QueryParser.parse(query) match {

      // query parsed successfully, time to execute it!
      case Success(queryAst) =>
        Await.result(executor.execute(queryAst = queryAst), _awaitTimeout)

      // can't parse GraphQL query, return error
      case Failure(error) => error.getMessage
    }
}
