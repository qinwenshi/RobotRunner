package robot;

import javax.tools.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Main {
    static String SRC_FOLDER = "/Users/qinwenshi/Desktop/java_exp/";

    public static void main(String[] args) throws IOException, InterruptedException {
        validateParameters(args);

        SRC_FOLDER = args[0];
        compileSourceCode(SRC_FOLDER);
        runRobot(SRC_FOLDER);
    }

    private static void validateParameters(String[] args) throws InterruptedException {
        if(args.length < 1)
            throw new InterruptedException("Usage: Need to pass in root folder for source code");
    }

    private static void compileSourceCode(String sourceFolder) throws IOException {
        File srcFolder = new File(sourceFolder);
        String[] compilerOptions = new String[]{"-d", sourceFolder + "out"};
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        Iterable<? extends JavaFileObject> compilationUnits = fileManager
                .getJavaFileObjectsFromStrings(retrieveSourceFiles(srcFolder));
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, Arrays.asList(compilerOptions),
                null, compilationUnits);
        if(!diagnostics.getDiagnostics().isEmpty())
            System.out.println(diagnostics.getDiagnostics().get(0).getMessage(new Locale("UTF-8")));
        task.call();
        fileManager.close();
    }

    private static List<String> retrieveSourceFiles(File folder) {
        List<String> files = new ArrayList<String>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                files.addAll(retrieveSourceFiles(fileEntry));
            } else {
                appendOnlyJavaSourceFile(files, fileEntry);
            }
        }
        return files;
    }

    private static void appendOnlyJavaSourceFile(List<String> files, File fileEntry) {
        if (fileEntry.getName().contains(".java")) {
            files.add(fileEntry.getAbsolutePath());
        }
    }

    private static void runRobot(String sourceFolder) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec("java -classpath " + sourceFolder + "out" + " Robot");
        InputStream stderr = proc.getInputStream();
        InputStreamReader isr = new InputStreamReader(stderr);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null)
            System.out.println(line);
        System.out.println("");
        proc.waitFor();
    }
}
