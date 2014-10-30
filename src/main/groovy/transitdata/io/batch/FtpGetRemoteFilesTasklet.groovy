package transitdata.io.batch

import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.InitializingBean
import org.springframework.integration.file.filters.SimplePatternFileListFilter
import org.springframework.integration.file.remote.session.SessionFactory
import org.springframework.integration.file.remote.synchronizer.AbstractInboundFileSynchronizer
import org.springframework.integration.ftp.filters.FtpSimplePatternFileListFilter
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizer
import org.springframework.util.Assert

class FtpGetRemoteFilesTasklet implements Tasklet, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(FtpGetRemoteFilesTasklet)
    private File localDirectory
    private AbstractInboundFileSynchronizer<?> ftpInboundFileSynchronizer
    private SessionFactory sessionFactory
    private boolean autoCreateLocalDirectory = true
    private boolean deleteLocalFiles = true
    private String fileNamePattern
    private String remoteDirectory
    private int downloadFileAttempts = 12
    private long retryIntervalMilliseconds = 300000
    private boolean retryIfNotFound = false

    void afterPropertiesSet() throws Exception {
        Assert.notNull(sessionFactory, 'sessionFactory attribute cannot be null')
        Assert.notNull(localDirectory, 'localDirectory attribute cannot be null')
        Assert.notNull(remoteDirectory, 'remoteDirectory attribute cannot be null')
        Assert.notNull(fileNamePattern, 'fileNamePattern attribute cannot be null')

        setupFileSynchronizer()

        if (!localDirectory.exists()) {
            if (autoCreateLocalDirectory) {
                if (logger.isDebugEnabled()) {
                    logger.debug('The {} directory doesn\'t exist Will create.', localDirectory )
                }
                localDirectory.mkdirs()
            }
            else {
                throw new FileNotFoundException(localDirectory.name)
            }
        }
    }

    private void setupFileSynchronizer() {
        ftpInboundFileSynchronizer = new FtpInboundFileSynchronizer(sessionFactory)
        ((FtpInboundFileSynchronizer) ftpInboundFileSynchronizer).filter =
                new FtpSimplePatternFileListFilter(fileNamePattern)
        ftpInboundFileSynchronizer.remoteDirectory = remoteDirectory
    }

    private void deleteLocalFiles() {
        if (deleteLocalFiles) {
            SimplePatternFileListFilter filter = new SimplePatternFileListFilter(fileNamePattern)
            List<File> matchingFiles = filter.filterFiles(localDirectory.listFiles())
            if (CollectionUtils.isNotEmpty(matchingFiles)) {
                for (File file : matchingFiles) {
                    FileUtils.deleteQuietly(file)
                }
            }
        }
    }

    @Override
    RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        deleteLocalFiles()

        ftpInboundFileSynchronizer.synchronizeToLocalDirectory(localDirectory)

        if (retryIfNotFound) {
            SimplePatternFileListFilter filter = new SimplePatternFileListFilter(fileNamePattern)
            int attemptCount = 1
            while (filter.filterFiles(localDirectory.listFiles()).size() == 0 && attemptCount <= downloadFileAttempts) {
                logger.info('File(s) matching {} not found on remote site. Attempt {} out of {}',
                        fileNamePattern, attemptCount, downloadFileAttempts)
                Thread.sleep(retryIntervalMilliseconds)
                ftpInboundFileSynchronizer.synchronizeToLocalDirectory(localDirectory)
                attemptCount++
            }

            if (attemptCount >= downloadFileAttempts && filter.filterFiles(localDirectory.listFiles()).size() == 0) {
                throw new FileNotFoundException('Could not find remote file(s) matching {} after {} attempts.',
                        fileNamePattern, downloadFileAttempts)
            }
        }

        return null
    }
}
