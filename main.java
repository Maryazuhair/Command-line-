import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.nio.file.StandardCopyOption;
class Parser {
    String commandName;
    String[] args;

    // This method will divide the input into commandName and args
    // where "input" is the string command entered by the user
    public boolean parse(String input)
    {
        //split to get individual string
        String[] str = input.split(" ");
        //check if there are no strings
        if (str.length == 0)
        {
            System.out.println("Invalid command! ");
            return false; //parsing failure
        }
        commandName = str[0]; //first string assumed command name
        args = new String[str.length - 1]; // The rest of the strings (if any) are considered as arguments
        System.arraycopy(str, 1, args, 0, str.length - 1);// Copy the arguments from string into the args array
        if(( commandName.equals("pwd")) && args.length != 0)
        {
            System.out.println("ERROR: " + commandName + " not take any argumnent!");
            return false;
        }
        if(( commandName.equals("rmdir") ||commandName.equals("echo") || commandName.equals("mkdir") ||commandName.equals("cat") ) && args.length == 0 )
        {
            System.out.println("ERROR: " + commandName + " take at least 1 argument!");
            return false;
        }
        if(( commandName.equals("touch")
                ||commandName.equals("rm") ) && args.length != 1 )
        {
            System.out.println("ERROR: " + commandName + " take one argument!");
            return false;
        }
        return true;//parsing successful
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }
}

 class Terminal

{
    Parser parser;
    private static Path currDirectory = Paths.get(System.getProperty("user.dir"));// current working directory
    List<String> histComm = new ArrayList<>(); // for storing history of commands
    // Constructor
    public Terminal()
    {
        this.parser = new Parser();
    }
    // This function is used to print text or messages
    public void echo(String[] str)
    {

        for (String arg : str)
        {
            System.out.print(arg + " "); // print each argument separated by a space
        }
        System.out.println();
    }
    // This function prints the current working directory
    public void pwd()
    {
        System.out.println(currDirectory.toAbsolutePath());
    }
    // this function to create a new directory.
    public void mkdir(String[] drc)
    {
        for (String directory : drc)
        {
            // Create a Path object representing the directory
            Path newDirectory = Paths.get(directory);
            //check if the new Directory is already exist
            if (Files.exists(newDirectory))
            {
                System.out.println("Directory already exists: " + newDirectory.toString());
            } else {
                try {
                    Files.createDirectories(newDirectory);//create nonexistent directory
                    System.out.println("Directory created: " + newDirectory.toString());
                } catch (Exception exp) {
                    System.out.println("Error creating directory: " + exp.getMessage());
                }
            }
        }
    }

    // this function for remove directories
    public void rmdir(String drc) {
        try {
            // Check if the provided directory is "*"
            if (drc.equals("*"))
            {
                // List all directories in the current directory
                Files.list(currDirectory)
                        .filter(Files::isDirectory)
                        .forEach(directory -> {
                            try {
                                // Check if the directory is empty
                                if (Files.list(directory).findFirst().isEmpty()) {
                                    // If empty, delete the directory
                                    Files.delete(directory);
                                    System.out.println("Directory removed: " + directory.toString());
                                }
                            } catch (IOException exp)
                            {
                                System.out.println("error removing directory: " + exp.getMessage());
                            }
                        });
            } else {

                Path directory = Paths.get(drc);

                if (Files.isDirectory(directory)) {
                    // Check if the directory is not empty
                    if (Files.list(directory).findFirst().isPresent()) {
                        // Directory is not empty, cannot be removed
                        System.out.println("error: Directory isn't empty.");
                    } else {
                        // Directory is empty, attempt to delete it
                        Files.delete(directory);
                        System.out.println("Directory removed: " + directory.toString());
                    }
                } else {
                    // The provided path is not a directory
                    System.out.println("error: Not a directory.");
                }
            }
        } catch (IOException exp) {
            // Handle any IOExceptions that occur during the process
            System.out.println("error: " + exp.getMessage());
        }
    }

    //this function to display command history
    public void history()
    {
        for (int var = 0; var < histComm.size(); var++) {
            System.out.println((var + 1) + " " + histComm.get(var));
        }
    }
    public void ls() {
        try {
            // List the contents of the current directory
            Files.list(currDirectory)
                    .map(path -> path.getFileName().toString()) // Extract file/directory names
                    .sorted() // Sort alphabetically
                    .forEach(System.out::println);
        } catch (IOException e) {
            System.out.println("Error listing files and directories: " + e.getMessage());
        }
    }

    public void lsReverse() {
        try {
            // List the contents of the current directory in reverse order
            Path[] filesAndDirs = Files.list(currDirectory)
                    .map(Path::getFileName)
                    .sorted((p1, p2) -> p2.toString().compareTo(p1.toString())) // Sort in reverse order
                    .toArray(Path[]::new);

            for (Path path : filesAndDirs) {
                System.out.println(path.getFileName());
            }
        } catch (IOException e) {
            System.out.println("Error listing files and directories in reverse order: " + e.getMessage());
        }
    }
    public void cp(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: cp <source_file> <destination_file>");
            return;
        }

        Path sourceFile = Paths.get(args[0]);
        Path destinationFile = Paths.get(args[1]);

        try {
            Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File copied successfully.");
        } catch (IOException e) {
            System.err.println("Error copying the file: " + e.getMessage());
        }
    }
    public void cpR(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: cp -r <source_directory> <destination_directory>");
            return;
        }

        Path sourceDirectory = Paths.get(args[0]);
        Path destinationDirectory = Paths.get(args[1]);

        if (!Files.exists(sourceDirectory) || !Files.isDirectory(sourceDirectory)) {
            System.err.println("Source directory does not exist or is not a directory.");
            return;
        }

        if (!Files.exists(destinationDirectory) || !Files.isDirectory(destinationDirectory)) {
            System.err.println("Destination directory does not exist or is not a directory.");
            return;
        }

        try {
            Files.walk(sourceDirectory)
                    .forEach(sourcePath -> {
                        Path relativePath = sourceDirectory.relativize(sourcePath);
                        Path destinationPath = destinationDirectory.resolve(relativePath);

                        try {
                            if (Files.isDirectory(sourcePath)) {
                                Files.createDirectories(destinationPath);
                            } else {
                                Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            System.err.println("Error copying the file/directory: " + e.getMessage());
                        }
                    });

            System.out.println("Directory and its contents copied successfully.");
        } catch (IOException e) {
            System.err.println("Error copying the directory: " + e.getMessage());
        }
    }


    //this function to create file
    public void touch(String []args)
    {
        File file;
        if(args[0].contains(":"))///////////////
        {
            file=new File(args[0]);
        }
        else
        {
            file=new File(currDirectory+ "\\" + args[0]);
        }
        try
        {
            if(! new File(args[0]).exists())
            {
                file.createNewFile();
            }
            else
            {
                System.out.println("File already exists");
            }
        }
        catch(IOException e)
        {
            System.out.println(e);
        }
    }
    //this function to remove file
    public void rm(String [] fileName)
    {
        File file = new File(fileName[0]);
        if(file.exists())
        {
            file.delete();
        }
        else if(!file.exists())
        {
            System.out.println("file does not exist");
        }
    }
    //this function to create directory
    public void cd(String []args)
    {
        if(args.length == 0)
        {
            currDirectory=  Paths.get(System.getProperty("user.home"));
        }
        else if(args.length==1 && args[0].equals(".."))
        {
            currDirectory = currDirectory.getParent();
        }
        else if(args[0].contains(":"))
        {
            Path newDir = Paths.get(System.getProperty("user.dir"));
            currDirectory =newDir;
        }
        else
        {
            System.out.println("ERROR");
        }
    }
    //this function to print content of one or two files
    public void cat(String [] args)
    {
        try
        {
            for (int i = 0; i < args.length; i++)
            {
                File file = new File(args[i]);
                Scanner fileReader = new Scanner(file);
                while (fileReader.hasNextLine())
                {
                    String data = fileReader.nextLine();
                    System.out.println(data);

                }
                fileReader.close();
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    //This method will choose the suitable command method to be called
    public void chooseCommandAction(String ch, String input)
    {
        switch (ch)
        {
            case "echo":
                if (parser.parse(input))
                {
                    echo(parser.getArgs());
                }
                break;
            case "pwd":
                if(parser.parse(input))
                {
                    pwd();
                }
                break;
            case "history":
                history();
                break;
            case "mkdir":
                if (parser.parse(input))
                {
                    String[] args = parser.getArgs();
                    mkdir(args);
                }
                break;
            case "rmdir":
                if (parser.parse(input))
                {
                    String[] args = parser.getArgs();
                    rmdir(args[0]);
                }
                break;
            case "touch":
                if (parser.parse(input))
                {
                    touch(parser.getArgs());
                }
                break;
            case "rm":
                if (parser.parse(input))
                {
                    rm(parser.getArgs());
                }
                break;
            case "cat":
                if (parser.parse(input))
                {
                    cat(parser.getArgs());
                }
                break;
            case "cd":
                if (parser.parse(input))
                {
                    cd(parser.getArgs());
                }
                break;
            case "ls":
                ls();
                break;
            case "ls-r":
                lsReverse();
                break;

            case "cp":
                if (parser.parse(input)) {
                    String[] args = parser.getArgs();
                    cp(args);
                }
                break;
            case "cp-r":
                if (parser.parse(input)) {
                    String[] args = parser.getArgs();
                    cpR(args);
                }
                break;
            case "exit":
                break;
            default:
                System.out.println("command not found");

        }
    }
    public static void main(String[] args)
    {
        Terminal terminal = new Terminal();
        Scanner scanner = new Scanner(System.in);

        while (true)
        {
            System.out.print("> ");
            String input = scanner.nextLine();

            if (input.equals("exit") )
            {
                break;
            }

            terminal.histComm.add(input);

            String[] inputParts = input.split(" ");
            String command = inputParts[0];
            String[] arguments = new String[inputParts.length - 1];
            System.arraycopy(inputParts, 1, arguments, 0, arguments.length);

            terminal.chooseCommandAction(command, input);
        }
        scanner.close();
    }

}
public class Main {
    {
        Terminal terminal = new Terminal();
        Scanner scanner = new Scanner(System.in);

        while (true)
        {
            System.out.print("> ");
            String input = scanner.nextLine();

            if (input.equals("exit") )
            {
                break;
            }

            terminal.histComm.add(input);

            String[] inputParts = input.split(" ");
            String command = inputParts[0];
            String[] arguments = new String[inputParts.length - 1];
            System.arraycopy(inputParts, 1, arguments, 0, arguments.length);

            terminal.chooseCommandAction(command, input);
        }
        scanner.close();
    }
    }
