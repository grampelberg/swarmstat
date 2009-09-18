package org.saunter.swarmstat.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import _root_.net.liftweb.util._

/**
 * The singleton that has methods for accessing the database
 */
object User extends User with MetaMegaProtoUser[User] {
  override def dbTableName = "users" // define the DB table name
  override def screenWrap = Full(<lift:surround with="default" at="content">
			       <lift:bind /></lift:surround>)
  // define the order fields will appear in forms and output
  override def fieldOrder = List(id, firstName, lastName, email,
  locale, timezone, password, textArea)

  // comment this line out to require email validations
  override def skipEmailValidation = true

  // If the server run.mode==dev, any connection is logged in.
  override def loggedIn_? =
    currentUserId.isDefined || System.getProperty("run.mode") == "dev"

  // Either you've got an account or not sucker.
  override lazy val sitemap: List[Menu] =
    List(loginMenuLoc, logoutMenuLoc).flatten(a => a)
}

/**
 * An O-R mapped "User" class that includes first name, last name, password and we add a "Personal Essay" to it
 */
class User extends MegaProtoUser[User] {
  def getSingleton = User // what's the "meta" server

  // define an additional field for a personal essay
  object textArea extends MappedTextarea(this, 2048) {
    override def textareaRows  = 10
    override def textareaCols = 50
    override def displayName = "Personal Essay"
  }


}
