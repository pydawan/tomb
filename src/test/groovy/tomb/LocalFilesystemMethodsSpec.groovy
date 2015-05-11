package tomb

import spock.lang.*
import java.nio.file.Path
import java.nio.file.Paths

class LocalFilesystemMethodsSpec extends Specification {

    Path tmpPath = Paths.get(System.getProperty('java.io.tmpdir'))
    LocalFilesystem fs = new LocalFilesystem(tmpPath)

    void 'Resolving a relative path'() {
        given: 'A relative path'
            def relativePath = Paths.get('images/relative/logo.png')

        when: 'resolving it'
            def result = fs.resolve(relativePath)

        then: 'the result should be the expected'
            result.toString() == "${tmpPath}/${relativePath}"
    }

    void 'Check if a file exists'() {
        given: 'A relative path'
            def tmpFile = File.createTempFile('tomb_', '_tmp')
            def relativePath = tmpFile.toPath()

        and: 'being sure that it exists'
            assert tmpFile.exists()

        when: 'checking if it exists'
            def result = fs.exists(relativePath)

        then: 'assert that is true'
            result
    }

    void "Check that a file doesn't exist"() {
        given: 'A relative path'
            def relativePath = Paths.get('/asdasd')

        and: "being sure that it doesn't exists"
            assert !relativePath.toFile().exists()

        when: 'checking if it exists'
            def result = fs.exists(relativePath)

        then: 'assert that is true'
            !result
    }

    void 'Obtaining a file'() {
        given: 'A filename'
            def filename = 'temp.txt'

        and: 'a file in the filesystem'
            def filePath = Paths.get("${fs.basePath}/${filename}")
            def f = filePath.toFile()
            f.text = 'holamundo'

        and: 'that exists'
            assert f.exists()

        when: 'obtaining the file'
            def result = fs.get(filePath)

        then: 'the contents of the file should be equivalent to the result'
            result.text == f.text
    }

    void "Obtaining a file that doesn't exist"() {
        given: 'A file in the filesystem'
            def f = File.createTempFile('tomb_', '_tmp')

        and: "that doesn't exist"
            f.delete()
            assert !f.exists()

        when: 'obtaining the file'
            fs.get(f.toPath())

        then: 'an exception should be thrown'
            thrown FilesystemException
    }

    void 'Obtaining a directory'() {
        given: 'A directory filesystem'
            def f = File.createTempDir('tomb_', '_tmp')

        and: 'that exist'
            assert f.exists()

        when: 'obtaining the file'
            fs.get(f.toPath())

        then: 'an exception should be thrown'
            thrown FilesystemException
    }

    void 'Uploading a file'() {
        given: 'A file'
            def f = File.createTempFile('tomb_', '_tmp')
            f.text = 'holamundo'

        and: 'a remote path'
            def relativePath = Paths.get('myfile')

        when: 'uploading it to the filesystem'
            fs.put(f.newInputStream(), relativePath)

        then: 'the file should be correctly uploaded'
            new File("${tmpPath}/${relativePath}").text == 'holamundo'

        cleanup: 'deleting the temporal resources created in the filesystem'
            new File("${tmpPath}/${relativePath}").delete()
    }

    void "Listing a remote filesystem's directory"() {
        given: 'A temporal directory'
            def tmpDir = File.createTempDir()

        and: 'two temporal files in it'
            new File(tmpDir, 'file1').createNewFile()
            new File(tmpDir, 'file2').createNewFile()

        when: 'listing the temporal directory contents'
            def result = fs.list(Paths.get(tmpDir.name))

        then: 'the result should contain our two files'
            result.size() == 2
            result.every { it.startsWith('file') }
    }

}
