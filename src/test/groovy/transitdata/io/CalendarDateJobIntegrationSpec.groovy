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
import transitdata.io.domain.CalendarDate

@ContextConfiguration('/jobs/calendardate-job.xml')
@TestExecutionListeners([
        DependencyInjectionTestExecutionListener,
        StepScopeTestExecutionListener
])
class CalendarDateJobIntegrationSpec extends Specification {

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
        CalendarDate expectedCalendarDate = new CalendarDate(
                service_id: 'AUG14-MVS-BUS-Weekday-01',
                date: '20141127',
                exception_type: 2
        )

        when: 'job is run'
        def jobExecution = jobLauncher.launchJob()
        List<CalendarDate> calendarDateList = jdbcTemplate.queryForList('select * from calendar_date')

        then: 'it completes successfully (execution is synchronous, so we don\'t have to wait for completion)'
        jobExecution.exitStatus == ExitStatus.COMPLETED

        and:
        calendarDateList.size() == 26
        calendarDateList.first().service_id == expectedCalendarDate.service_id
        calendarDateList.first().date == expectedCalendarDate.date
        calendarDateList.first().exception_type == expectedCalendarDate.exception_type
    }
}
