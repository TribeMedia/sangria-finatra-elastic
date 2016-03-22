package graphQL

import models.{Customer, CustomerCreationModel}
import sangria.schema.{Field, _}
import services.ElasticSearchService

/**
  * Created by gqadonis on 3/22/16.
  */
object SchemaDefinition {
  val CustomerType = ObjectType(
    "customer",
    "An entity of customer.",
    fields[Unit, Customer](
      Field("id", StringType,
        Some("The id of the customer"),
        resolve = _.value._id),
      Field("lastName", StringType,
        Some("Last name of customer."),
        resolve = _.value.lastName),
      Field("firstName", StringType,
        Some("First name of customer."),
        resolve = _.value.firstName),
      Field("ssn", StringType,
        Some("SSN of customer."),
        resolve = _.value.ssn)
    )
  )

  val ID = Argument("id", StringType, description = "id of the customer")
  val LastNameArg = Argument("lastName", StringType, description = "last name of the customer")
  val FirstNameArg = Argument("firstName", StringType, description = "first name of the customer")
  val SSNArg =  Argument("ssn", StringType, description = "ssn of the customer")
  val KeywordArg = Argument("keyword", OptionInputType(StringType), description = "keyword of filtering customers")

  val Query = ObjectType(
    "Query", fields[ElasticSearchService, Unit](
      Field("customer", OptionType(CustomerType),
        arguments = ID :: Nil,
        resolve = ctx => ctx.ctx.getCustomer(ctx arg ID)) ,
      Field("customers", ListType(CustomerType),
        arguments = KeywordArg :: Nil,
        resolve = ctx => ctx.ctx.getMatchingCustomers(ctx argOpt KeywordArg))
    ))

  val Mutation = ObjectType("MutationRoot", fields[ElasticSearchService, Unit](
    Field("addCustomer", OptionType(CustomerType),
      arguments = LastNameArg :: FirstNameArg :: SSNArg :: Nil,
      resolve = ctx => ctx.ctx.addCustomer(
        CustomerCreationModel(ctx arg LastNameArg, ctx arg FirstNameArg, ctx arg SSNArg))),
    Field("updateCustomer", OptionType(CustomerType),
      arguments = ID :: LastNameArg :: FirstNameArg :: SSNArg :: Nil,
      resolve = ctx => ctx.ctx.updateCustomer(Customer(ctx arg ID, ctx arg LastNameArg, ctx arg FirstNameArg, ctx arg SSNArg))),
    Field("deleteCustomer", OptionType(IDType),
      arguments = ID :: Nil,
      resolve = ctx => ctx.ctx.deleteCustomer(ctx arg ID))
  ))

  val CustomerSchema = Schema(Query, Some(Mutation))
}
