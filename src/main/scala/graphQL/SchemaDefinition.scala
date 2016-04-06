package graphQL

import com.sksamuel.elastic4s.mappings.FieldType.DateType
import models.{Quote, QuoteCreationModel}
import sangria.schema.{Field, _}
import services.ElasticSearchService

/**
  * Created by gqadonis on 3/22/16.
  */
object SchemaDefinition {
  val QuoteType = ObjectType(
    "quote",
    "An entity of quote.",
    fields[Unit, Quote](
      Field("id", StringType,
        Some("The ID of the quote."),
        resolve = _.value.a2z),
      Field("a2z", StringType,
        Some("The first letter of the last name."),
        resolve = _.value.a2z),
      Field("lastname", StringType,
        Some("Last name of author."),
        resolve = _.value.lastname),
      Field("author", StringType,
        Some("Full name of author."),
        resolve = _.value.author),
      Field("profession", StringType,
        Some("Profession of author."),
        resolve = _.value.profession),
      Field("nationality", StringType,
        Some("Nationality of author."),
        resolve = _.value.nationality),
      Field("deathdate", StringType,
        Some("Death date of author."),
        resolve = _.value.deathdate),
      Field("quote", StringType,
        Some("Quote of author."),
        resolve = _.value.quote),
      Field("keywords", StringType,
        Some("Keywords of quote."),
        resolve = _.value.keywords)
    )
  )

  val ID = Argument("id", StringType, description = "id of the quote")
  val A2ZArg = Argument("a2z", StringType, description = "first letter of last name of author")
  val LastNameArg = Argument("lastName", StringType, description = "last name of the author")
  val AuthorArg = Argument("firstName", StringType, description = "full name of author")
  val ProfessionArg =  Argument("profession", StringType, description = "profession of the customer")
  val NationalityArg =  Argument("nationality", StringType, description = "nationality of the customer")
  val BirthdateArg =  Argument("birthdate", StringType, description = "profession of the customer")
  val DeathdateArg =  Argument("deathdate", StringType, description = "profession of the customer")
  val QuoteArg =  Argument("quote", StringType, description = "quote text")
  val KeywordsArg =  Argument("keywords", StringType, description = "keywords for quote")
  val KeywordArg = Argument("keyword", OptionInputType(StringType), description = "keyword of filtering quotes")

  val Query = ObjectType(
    "Query", fields[ElasticSearchService, Unit](
      Field("quote", OptionType(QuoteType),
        arguments = ID :: Nil,
        resolve = ctx => ctx.ctx.getItem(ctx arg ID)) ,
      Field("quotes", ListType(QuoteType),
        arguments = KeywordArg :: Nil,
        resolve = ctx => ctx.ctx.getMatchingItems(ctx argOpt KeywordArg))
    ))

  val Mutation = ObjectType("MutationRoot", fields[ElasticSearchService, Unit](
    Field("addItem", OptionType(QuoteType),
      arguments = A2ZArg :: LastNameArg :: AuthorArg :: ProfessionArg :: NationalityArg :: Nil,
      resolve = ctx => ctx.ctx.addItem(
        QuoteCreationModel(ctx arg A2ZArg, ctx arg LastNameArg, ctx arg AuthorArg, ctx arg ProfessionArg, ctx arg NationalityArg,
          ctx arg BirthdateArg, ctx arg DeathdateArg, ctx arg QuoteArg, ctx arg KeywordsArg))),
    Field("updateItem", OptionType(QuoteType),
      arguments = ID :: LastNameArg :: AuthorArg :: ProfessionArg :: NationalityArg :: BirthdateArg :: DeathdateArg :: QuoteArg :: KeywordsArg :: Nil,
      resolve = ctx => ctx.ctx.updateItem(Quote(ctx arg ID, ctx arg A2ZArg, ctx arg LastNameArg, ctx arg AuthorArg, ctx arg ProfessionArg, ctx arg NationalityArg,
        ctx arg BirthdateArg, ctx arg DeathdateArg, ctx arg QuoteArg, ctx arg KeywordsArg))),
    Field("deleteItem", OptionType(IDType),
      arguments = ID :: Nil,
      resolve = ctx => ctx.ctx.deleteItem(ctx arg ID))
  ))

  val QuoteSchema = Schema(Query, Some(Mutation))
}
