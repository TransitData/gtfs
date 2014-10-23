package transitdata.io

import com.jolbox.bonecp.BoneCPDataSource
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.StepScopeTestExecutionListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import spock.lang.Specification
import transitdata.io.domain.Shape

@ContextConfiguration('/jobs/shapes-job.xml')
@TestExecutionListeners([
        DependencyInjectionTestExecutionListener,
        StepScopeTestExecutionListener
])
class ShapeJobIntegrationSpec extends Specification {

    @Autowired
    JobLauncherTestUtils jobLauncher

    @Autowired
    BoneCPDataSource dataSource

    JdbcTemplate jdbcTemplate

    def setup() {
        jdbcTemplate = new JdbcTemplate(dataSource)
    }

    void 'simple properties injection'() {
        expect:
        jobLauncher != null
    }

    void 'launch job'() {
        given:
        Shape expectedShape = new Shape(
                transit_system : 'METRO_TRANSIT',
                shape_id : '20001',
                shape_pt_lat : '44.9617268795',
                shape_pt_lon : '-93.2921253393',
                shape_pt_sequence : 10001,
                shape_dist_traveled : null
        )

        when: 'job is run'
        def jobExecution = jobLauncher.launchJob()
        List<Shape> shapeList = jdbcTemplate.queryForList('select * from shape')

        then: 'it completes successfully (execution is synchronous, so we don\'t have to wait for completion)'
        jobExecution.exitStatus == ExitStatus.COMPLETED

        and:
        shapeList.size() == 18
        shapeList.first().transit_system == expectedShape.transit_system
        shapeList.first().shape_id == expectedShape.shape_id
        shapeList.first().shape_pt_lat as String == expectedShape.shape_pt_lat
        shapeList.first().shape_pt_lon as String == expectedShape.shape_pt_lon
        shapeList.first().shape_pt_sequence == expectedShape.shape_pt_sequence
        shapeList.first().shape_dist_traveled == expectedShape.shape_dist_traveled

    }
}
