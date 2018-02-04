import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.Scanner;
import java.lang.Runtime;
import java.lang.Process;
import java.lang.NumberFormatException;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Date;

public class FileCountMonitor
{
	private static final String ARCHIVE_EXTENSION	= ".zip";
	private static final String LOG_FILE			= "FileCountMonitor.log";
	
	private static int maxZipCount = 0;
	
	private static String rootDirectory 	= "";
	private static String backLogDirectory = "";
	
	public static void main(String args[])
	{
		boolean inputCorrect = false;
		
		try
		{
			if(args.length == 3)
			{
				File tmpRoot 	= new File(args[0]);
				File tmpBacklog = new File(args[1]);
				
				if(tmpRoot.isDirectory() && tmpBacklog.isDirectory())
				{
					inputCorrect = true;
				}
				else
				{
					log("Arguments 2 and 3 must be existing directories!");
				}
				
				if(inputCorrect)
				{
					try
					{
						maxZipCount = Integer.valueOf(args[2]).intValue();
					}
					catch(NumberFormatException nfe)
					{
						inputCorrect = false;
						log("Arg. 3 must be a number!");
					}
				}
			}
			
			if(inputCorrect)
			{
				rootDirectory 	 = args[0];
				backLogDirectory = args[1];
				
				rootDirectory 	 = addTrailingBackSlash(rootDirectory);
				backLogDirectory = addTrailingBackSlash(backLogDirectory);
			
				log("--Checking For Excess Files--");
				
				log("Maximum File Count: " + maxZipCount);
				log("Root Directory: " + rootDirectory);
				log("Backlog Directory: " + backLogDirectory);
				
				ArrayList<File> allFiles 	 = listFiles(rootDirectory);
				ArrayList<File> backlogFiles = listFiles(backLogDirectory);
				
				int rootFileCount = allFiles.size();
				
				log("Files in root directory: " + rootFileCount);
				
				allFiles.addAll(backlogFiles);
				
				int filesInBacklog  = backlogFiles.size();
				int totalFileCount	= allFiles.size();
				
				log("Files back logged: " + filesInBacklog);
				log("-");
				
				sort(allFiles);
				
				int stopAt = maxZipCount;
				if(maxZipCount > totalFileCount)
				{
					stopAt = totalFileCount;
				}
				
				for(int i = 0; i < totalFileCount; i++)
				{
					File f = allFiles.get(i);
					
					if(backlogFiles.contains(f) && i < stopAt)
					{	
						String oldPath = f.getPath();
						String newPath = rootDirectory + f.getName();
						
						if(!oldPath.equalsIgnoreCase(newPath))
						{
							log("Moving " + f.getName() + " to " + rootDirectory);
							log("-Move Successful? " + moveFile(oldPath, newPath));
						}
					}
					else if(i >= stopAt)
					{
						String oldPath = f.getPath();
						String newPath = backLogDirectory + f.getName();
						
						if(!oldPath.equalsIgnoreCase(newPath))
						{
							log("Moving " + f.getName() + " to " + backLogDirectory);
							log("-Move Successful? " + moveFile(oldPath, newPath));
						}
					}
				}
				log("--Check Complete--");
			}
			else
			{
				log("Bad Input!");
			}
		}
		catch(Exception e)
		{
			log(e);
		}
    }
	
	private static void log(Exception e)
	{
		StackTraceElement[] stackTrace = e.getStackTrace();
		
		String stackTraceString = e.getMessage();
		for(int i = 0; i < stackTrace.length; i++)
		{
			stackTraceString += "\n\t at " + stackTrace[i];
		}
		
		log(stackTraceString);
	}
	
	private static void log(String s)
	{
		System.out.println(s);
		try {
			File file = new File(rootDirectory + LOG_FILE);
 
			if (!file.exists())
			{
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			
			Date timestamp = new Date();
			bw.write(timestamp.toString() + ": ");
			
			bw.write(s);
			bw.newLine();

			bw.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void sort(ArrayList<File> a)
	{
		sort(a, 0, a.size()-1);
	}
	
	private static void sort(ArrayList<File> a, int p, int r)
	{
		if(p < r)
		{
			int q = partition(a, p, r);
			sort(a, p, q-1);
			sort(a, q+1, r);
		}
	}
	
	private static int partition(ArrayList<File> array, int p, int r)
	{
		File fileX = array.get(r);
		int i = p - 1;
		
		for(int j = p; j < r; j++)
		{
			File fileJ = array.get(j);
			if(fileJ.lastModified() < fileX.lastModified())
			{
				i += 1;
				File fileI = array.get(i);
				array.set(i, fileJ);
				array.set(j, fileI);
			}
		}
		File fileII = array.get(i+1);
		array.set(i+1, fileX);
		array.set(r, fileII);
		return i + 1;
	}
	
	private static ArrayList listFiles(String directory)
	{
		ArrayList fileList = null;
		try
		{
			fileList = new ArrayList();
		
			File file = new File(directory);
			File list[] = file.listFiles();
			
			if(list != null)
			{
				for(int i = 0; i < list.length; i++)
				{
					File tmp = list[i];
					String fileExtension = tmp.getName();
					if(tmp.isFile())
					{
						fileExtension = fileExtension.substring(fileExtension.length()-4);
						if(fileExtension.equalsIgnoreCase(ARCHIVE_EXTENSION))
						{
							fileList.add(tmp);
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			log(e);
		}
		return fileList;
	}
	
	private static boolean moveFile(String a, String b)
	{
		boolean result = false;
		
		InputStream aStream = null;
		OutputStream bStream = null;
		
    	try
		{
			File afile = new File(a);
			File bfile = new File(b);

			aStream = new FileInputStream(afile);
			bStream = new FileOutputStream(bfile);

			byte[] buffer = new byte[1024];

			int length;
			
			while ((length = aStream.read(buffer)) > 0){

				bStream.write(buffer, 0, length);
			}

			aStream.close();
			bStream.close();
			
			bfile.setLastModified(afile.lastModified());
			
			result = bfile.exists();
			
			afile.delete();
			
    	}
		catch(Exception e)
		{
    		log(e);
    	}
		return result;
	}
	
	private static String addTrailingBackSlash(String s)
	{
		int l = s.length();
		
		try
		{
			if(!s.substring(l-1,l).equals("\\"))
			{
				s += "\\";
			}
		}
		catch(Exception e)
		{
			log(e);
		}
		return s;
	}
}