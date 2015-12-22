/**
 * Created by rimukas on 10/12/15.
 */


package controllers

import com.google.inject.Inject
import play.api.cache.CacheApi
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent.Future
import squants.energy.{Gigajoules, KBtus, Energy, KilowattHours}

import models.EUIMetrics
import models.EUICalculator
import scala.util.control.NonFatal
import scala.util.{ Success, Failure, Try}

import scala.concurrent.ExecutionContext.Implicits.global


trait BaselineActions {
  this: Controller =>

  implicit def roundDouble(d:Double):Double = roundAt(2)(d)
  implicit def energyToJSValue(b: Energy): JsValue = Json.toJson(roundAt(1)(b.value))
  implicit def listEnergyToJSValue(v: List[Energy]): JsValue = Json.toJson(v.map{
    case e:Energy => roundAt(1)(e.value)
  })


  def roundAt(p: Int)(n: Double): Double = { val s = math pow (10, p); (math round n * s) / s }

  def apiRecover(throwable: Throwable): Either[String, JsValue] = {
    throwable match {
      case NonFatal(th) => Left(th.getMessage)
    }
  }

  def api(response: Any):Either[String, JsValue] = {
    response match {
      case v: Energy => Right(v)
      case v: Double => Right(Json.toJson(roundAt(2)(v)))
      case v: Int => Right(Json.toJson(v))
      case v: List[Energy] => Right(v)
      case None => Left("Could not recognize input type")
    }
  }

  def makeBaseline() = Action.async(parse.json) { implicit request =>
    val getBaseline: EUIMetrics = EUIMetrics(request.body)
    val energyCalcs: EUICalculator = EUICalculator(request.body)

    val fieldNames = Seq(
      "ES", "sourceEUI", "siteEUI", "totalSourceEnergy", "totalSiteEnergy",

      "targetES", "targetSourceEUI", "targetSiteEUI", "targetSourceEnergy", "targetSiteEnergy",

      "percentBetterES", "percentBetterSourceEUI", "percentBetterSiteEUI", "percentBetterSourceEnergy", "percentBetterSiteEnergy",

      "medianES", "medianSourceEUI", "medianSiteEUI", "medianSourceEnergy", "medianSiteEnergy",

      "sourceEnergy", "siteEnergy", "poolEnergy", "parkingEnergy", "totalSourceEnergyNoPoolNoParking")

    val futures = Future.sequence(Seq(
      // first column table output
      getBaseline.ES.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      getBaseline.sourceEUI.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      getBaseline.siteEUI.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      energyCalcs.getTotalSourceEnergy.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      energyCalcs.getTotalSiteEnergy.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},

      // second column table output -- Either user supplied ES Target, or %Better Score
      getBaseline.getTargetES.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      getBaseline.targetSourceEUI.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      getBaseline.targetSiteEUI.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      getBaseline.targetSourceEnergy.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      getBaseline.targetSiteEnergy.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},

      getBaseline.percentBetterES.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      getBaseline.percentBetterSourceEUI.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      getBaseline.percentBetterSiteEUI.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      getBaseline.percentBetterSourceEnergy.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      getBaseline.percentBetterSiteEnergy.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},

      // third column table output
      Future{api(50)},
      getBaseline.medianSourceEUI.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      getBaseline.medianSiteEUI.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      getBaseline.medianSourceEnergy.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      getBaseline.medianSiteEnergy.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},

      // extra information, e.g. site/source conversion breakdowns
      energyCalcs.getSourceEnergy.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      energyCalcs.getSiteEnergy.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      energyCalcs.getPoolEnergy.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      energyCalcs.getParkingEnergy.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)},
      energyCalcs.getTotalSourceEnergyNoPoolNoParking.map(api(_)).recover{ case NonFatal(th) => apiRecover(th)}
  ))

    futures.map(fieldNames.zip(_)).map { r =>
      val errors = r.collect {
        case (n, Left(s)) => Json.obj(n -> s)
      }
      val results = r.collect {
        case (n, Right(s)) => Json.obj(n -> s)
      }
      Ok(Json.obj(
        "values" -> results,
        "errors" -> errors
      ))
    }

  }
}
class BaselineController @Inject() (val cache: CacheApi) extends Controller with Security with Logging with BaselineActions