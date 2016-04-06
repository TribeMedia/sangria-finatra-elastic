package models

/**
  * Created by gqadonis on 4/6/16.
  */
case class Quote(_id: String, a2z: String, lastname: String, author: String, profession: String,
                 nationality: String, birthdate: String,
                 deathdate: String, quote: String, keywords: String) {

}

case class QuoteCreationModel(a2z: String, lastname: String, author: String, profession: String, nationality: String, birthdate: String,
                 deathdate: String, quote: String, keywords: String) {

}