package transitdata.io.batch

import org.apache.log4j.Logger

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import javax.annotation.PostConstruct

@Component
class FileUnzipperTasklet implements Tasklet {

    private static final Logger log = Logger.getLogger(FileUnzipperTasklet)

    @Autowired
    Environment env

    private String gtfsDirectory
    private String gtfsFilename

    @PostConstruct
    void init() {
        gtfsDirectory = env.getProperty('batch.ftp.pull.local.directory')
        gtfsFilename = env.getProperty('batch.ftp.pull.local.filename')
    }

    @Override
    RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        log.info("gtfsDirectory : $gtfsDirectory")
        log.info("gtfsFilename : $gtfsFilename")
        unZipIt("$gtfsDirectory/$gtfsFilename", gtfsDirectory)

        return RepeatStatus.FINISHED
    }

    /**
     * Unzip it
     * @param zipFile input zip file
     * @param output zip file output folder
     */
    void unZipIt(String zipFile, String outputFolder) {

        byte[] buffer = new byte[1024]

        try{

            //create output directory is not exists
            File folder = new File(outputFolder)
            if (!folder.exists()) {
                folder.mkdir()
            }

            //get the zip file content
            ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(zipFile))
            //get the zipped file list entry
            ZipEntry ze = zis.nextEntry

            while (ze != null) {

                String fileName = ze.name
                File newFile = new File(outputFolder + File.separator + fileName)

                log.info("file unzip : ${newFile.absoluteFile}")

                new File(newFile.parent).mkdirs()
                FileOutputStream fos = new FileOutputStream(newFile)

                int len
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len)
                }

                fos.close()
                ze = zis.nextEntry
            }

            zis.closeEntry()
            zis.close()

            log.info('Done')

        }
        catch (IOException ex) {
            log.error("io exception while extracting $gtfsFilename", ex)
        }
    }
}
