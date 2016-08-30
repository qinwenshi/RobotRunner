package robot;

import javax.tools.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Main {
    static String SRC_FOLDER = "/Users/qinwenshi/Desktop/java_exp/";
    static String GPIO_LIB_PATH = "/opt/pi4j/lib/*";
    private static String Robot_class_name;

    public static void main(String[] args) throws IOException, InterruptedException {
        validateParameters(args);

        SRC_FOLDER = args[0];
        Robot_class_name = args[1].split("\\.")[0];

        if(compileSourceCode(SRC_FOLDER))
            runRobot(SRC_FOLDER);
    }

    private static void validateParameters(String[] args) throws InterruptedException {
        if(args.length < 1)
            throw new InterruptedException("Usage: Need to pass in root folder for source code");
    }


    private static String buildClassPath(String... paths) {
        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            if (path.endsWith("*")) {
                path = path.substring(0, path.length() - 1);
                File pathFile = new File(path);
                for (File file : pathFile.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(".jar")) {
                        sb.append(path);
                        sb.append(file.getName());
                        sb.append(System.getProperty("path.separator"));
                    }
                }
            } else {
                sb.append(path);
                sb.append(System.getProperty("path.separator"));
            }
        }
        return sb.toString();
    }

    private static boolean compileSourceCode(String sourceFolder) throws IOException {

        String compilingClasspath = buildClassPath(GPIO_LIB_PATH);
        File srcFolder = new File(sourceFolder);
        String[] compilerOptions = new String[]{"-classpath", compilingClasspath, "-d", sourceFolder + "out"};

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();


        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        Iterable<? extends JavaFileObject> compilationUnits = fileManager
                .getJavaFileObjectsFromStrings(retrieveSourceFiles(srcFolder));
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, Arrays.asList(compilerOptions),
                null, compilationUnits);

        Boolean succ = task.call();
        if(!diagnostics.getDiagnostics().isEmpty())
            System.out.println(diagnostics.getDiagnostics().get(0).getMessage(new Locale("UTF-8")));

        fileManager.close();
        return succ;
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
        String runningClasspath = buildClassPath(sourceFolder+"out", GPIO_LIB_PATH);
        Process proc = rt.exec("java -classpath " + runningClasspath + " " + Robot_class_name);
        InputStream stdout = proc.getInputStream();
        InputStream stderr = proc.getErrorStream();
        InputStreamReader isr = new InputStreamReader(stdout);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null)
            System.out.println(line);
        isr = new InputStreamReader(stderr);
        br = new BufferedReader(isr);
        while ((line = br.readLine()) != null)
            System.out.println(line);
        System.out.println("");
        proc.waitFor();
    }
}
