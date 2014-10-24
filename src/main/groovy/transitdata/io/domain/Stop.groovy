package transitdata.io.domain

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
@CompileStatic
class Stop {

    String id
    String transit_system
    String stop_id
    String stop_code
    String stop_name
    String stop_desc
    Double stop_lat
    Double stop_lon
    String stop_street
    String stop_city
    String stop_region
    String stop_postcode
    String stop_country
    String zone_id
    String stop_url
    String location_type
    String parent_station
    String stop_timezone
    Integer wheelchair_boarding

}

