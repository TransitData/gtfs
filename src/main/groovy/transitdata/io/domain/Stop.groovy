package transitdata.io.domain

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
@CompileStatic
class Stop {
  String id
  String name
  String desc
  String lat
  String lon
  String street
  String city
  String region
  String postcode
  String country
  String zoneid
  String wheelchairboarding
  String url
}

