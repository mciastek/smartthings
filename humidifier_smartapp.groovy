definition(
    name: "Humidity plug auto switch",
    namespace: "mciastek",
    author: "Mirek Ciastek",
    description: "Turn on/off smart plug for given humidity and in given time range",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png",
    pausable: true
)

preferences {
  section("Monitor humidity") {
    input "humiditySensor1", "capability.relativeHumidityMeasurement", required: true
  }

  section("When humidity drops below") {
    input "humidity1", "number", title: "Humidity?", required: true
  }

  section("Turn on between what times?") {
    input "fromTime", "time", title: "From", required: false
    input "toTime", "time", title: "To", required: false
  }

  section("Toggle plug") {
    input "plug1", "capability.switch", required: true
  }
}

def installed() {
  log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
  log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
  triggerOnHumidity(humiditySensor1.humidityState)

  subscribe(humiditySensor1, "humidity", humidityHandler)
}

def humidityHandler(evt) {
  log.trace "humidity: $evt.value, $evt"

  triggerOnHumidity(evt)
}

private triggerOnHumidity(humidityState) {
  def minHumidity = humidity1
  def between = isBetween()

  if (humidityState.doubleValue < minHumidity) {
    log.debug "Humidity fallen below $minHumidity"

    if (between) {
      log.debug "Is within selected time range"
      turnOn()
    } else {
      log.debug "Is out of selected time range"
      turnOff()
    }
  } else {
    turnOff()
  }
}

private isBetween() {
  if (!fromTime && !toTime) {
    return true
  }

  return timeOfDayIsBetween(fromTime, toTime, new Date(), location.timeZone)
}

private turnOn() {
  log.debug "Turning on ${plug1.displayName}..."
  plug1.on()
}

private turnOff() {
  log.debug "Turning off ${plug1.displayName}..."
  plug1.off()
}
