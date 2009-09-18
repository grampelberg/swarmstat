package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.http._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import Helpers._
import _root_.net.liftweb.mapper.{DB, ConnectionManager, Schemifier, DefaultConnectionIdentifier, ConnectionIdentifier}
import _root_.java.sql.{Connection, DriverManager}
import _root_.javax.servlet.http.{HttpServletRequest}
import net.lag.configgy.Configgy
import net.lag.logging.Logger

import _root_.org.saunter.swarmstat.model._
import _root_.org.saunter.swarmstat.snippet._
import _root_.org.saunter.swarmstat.torrent._
import _root_.org.saunter.swarmstat.util._


/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  */
class Boot {
  /**
   * Setup logging
   */

  Configgy.configureFromResource("/logging.conf",
                                 getClass.getClassLoader)
  val log = Logger.get
  def boot {
    log.info("Booting.")
    if (!DB.jndiJdbcConnAvailable_?)
      DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)

    // where to search snippet
    LiftRules.addToPackages("org.saunter.swarmstat")
    Schemifier.schemify(true, Log.infoF _,
                        Peer, Relationship, Torrent, TorrentSource,
                        TorrentState, Tracker, User)

    LiftRules.setSiteMap(SiteMap(MenuSetup.menu:_*))

    /*
     * Show the spinny image when an Ajax call starts
     */
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    /*
     * Make the spinny image go away when it ends
     */
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append(makeUtf8)

    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    S.addAround(DB.buildLoanWrapper)

    /**
     * Startup the master feed watcher.
     */
    FeedWatcher
    StateWatcher

    log.info("Boot complete.")
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HttpServletRequest) {
    req.setCharacterEncoding("UTF-8")
  }
}

/**
* Database connection calculation
*/
object DBVendor extends ConnectionManager {
  private var pool: List[Connection] = Nil
  private var poolSize = 0
  private val maxPoolSize = 4

  private def createOne: Box[Connection] = try {
    val driverName: String = Props.get("db.driver") openOr
    "org.apache.derby.jdbc.EmbeddedDriver"

    val dbUrl: String = Props.get("db.url") openOr
    "jdbc:derby:swarmstat_db;create=true"

    Class.forName(driverName)

    val dm = (Props.get("db.user"), Props.get("db.password")) match {
      case (Full(user), Full(pwd)) =>
	DriverManager.getConnection(dbUrl, user, pwd)

      case _ => DriverManager.getConnection(dbUrl)
    }

    Full(dm)
  } catch {
    case e: Exception => e.printStackTrace; Empty
  }

  def newConnection(name: ConnectionIdentifier): Box[Connection] =
    synchronized {
      pool match {
	case Nil if poolSize < maxPoolSize =>
	  val ret = createOne
        poolSize = poolSize + 1
        ret.foreach(c => pool = c :: pool)
        ret

	case Nil => wait(1000L); newConnection(name)
	case x :: xs => try {
          x.setAutoCommit(false)
          Full(x)
        } catch {
          case e => try {
            pool = xs
            poolSize = poolSize - 1
            x.close
            newConnection(name)
          } catch {
            case e => newConnection(name)
          }
        }
      }
    }

  def releaseConnection(conn: Connection): Unit = synchronized {
    pool = conn :: pool
    notify
  }
}

object MenuSetup {

  def index: Box[Menu] =
    Full(Menu(Loc("Home", List("index"), S.?("Home"))))

  def state: Box[Menu] =
    Full(Menu(Loc("State", List("state"), S.?("State"), User.testLogginIn)))

  def manual: Box[Menu] =
    Full(Menu(Loc("Manual", List("manual"), S.?("Manual"), User.testLogginIn)))

  def menu: List[Menu] =
    List(index, state, manual).flatten(x=>x) ::: TorrentOverview.sitemap :::
    User.sitemap

}

