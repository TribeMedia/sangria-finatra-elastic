package models

/**
  * Created by gqadonis on 3/22/16.
  */
case class Customer(_id: String, firstName: String, lastName: String, ssn: String)

case class CustomerCreationModel(firstName: String, lastName: String, ssn: String)
