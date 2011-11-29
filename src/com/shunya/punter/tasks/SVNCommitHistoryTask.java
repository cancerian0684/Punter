package com.sapient.punter.tasks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.annotations.OutputParam;
import com.sapient.punter.annotations.PunterTask;
import com.sapient.punter.utils.StringUtils;

@PunterTask(author="munishc",name="SVNCommitHistoryTask",description="Takes out SVN Commit history.",documentation= "docs/docs/TextSamplerDemoHelp.html")
public class SVNCommitHistoryTask extends Tasks {
	@InputParam(required = true,description="Line separated Project Names")
	private String projectNames;
	@InputParam(required = true,description="Line separated Project URLs")
    private String urlList;
	@InputParam(required = true,description="SVN Username")
    private String username = "chandemu";
	@InputParam(required = true,description="SVN Password")
    private String password = "Dare11dream$";

	@OutputParam
	private String htmlOutput;

	@Override
	public boolean run() {
		boolean status=false;
		long startRevision = 0;
        long endRevision = -1;//HEAD (the latest) revision
        setupLibrary();
        String [] projects=projectNames.split("\n");
        String [] urls=urlList.split("\n");
        Revisions revisions=new Revisions();
        SVNRepository repository = null;
        try {
        for(int project_counter=0;project_counter<projects.length;project_counter++)
	    {
	       Project project =new Project();
	       repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(urls[project_counter]));
	       ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
	       repository.setAuthenticationManager(authManager);
	       endRevision = repository.getLatestRevision();
	       Collection logEntries = null;
	       Calendar cal=Calendar.getInstance();
	       cal.add(Calendar.DAY_OF_MONTH, -1);
       	   cal.set(Calendar.HOUR_OF_DAY, 0);
       	   startRevision=repository.getDatedRevision(cal.getTime());
           logEntries = repository.log(new String[] {""}, null, startRevision, endRevision, true, true);	       
	       for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
	            SVNLogEntry logEntry = (SVNLogEntry) entries.next();
	            if(logEntry.getDate().before(cal.getTime())){
	            	continue;
	            }
	            
	            /*System.out.println("---------------------------------------------");
	            System.out.println("revision: " + logEntry.getRevision());
	            System.out.println("author: " + logEntry.getAuthor());
	            System.out.println("date: " + logEntry.getDate());
	            System.out.println("log message: " + logEntry.getMessage());*/
	            String[] paths=new String [logEntry.getChangedPaths().size()];
	            if (logEntry.getChangedPaths().size() > 0) {
	//                System.out.println(logEntry.getChangedPaths().size());
	//                System.out.println("changed paths:");
	                
	                 /* keys are changed paths */
	                Set changedPathsSet = logEntry.getChangedPaths().keySet();
	                int count=0;
	                for (Iterator changedPaths = changedPathsSet.iterator(); changedPaths.hasNext();) {
	                    SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntry
	                            .getChangedPaths().get(changedPaths.next());
	                    
	                     /* SVNLogEntryPath.getPath returns the changed path itself;
	                     * 
	                     * SVNLogEntryPath.getType returns a charecter describing
	                     * how the path was changed ('A' - added, 'D' - deleted or
	                     * 'M' - modified);
	                     * 
	                     * If the path was copied from another one (branched) then
	                     * SVNLogEntryPath.getCopyPath &
	                     * SVNLogEntryPath.getCopyRevision tells where it was copied
	                     * from and what revision the origin path was at.
	                     * */
	                    paths[count]=entryPath.getPath();
	                    LOGGER.get().log(Level.INFO, " "
	                            + entryPath.getType()
	                            + "	"
	                            + entryPath.getPath()
	                            + ((entryPath.getCopyPath() != null) ? " (from "
	                                    + entryPath.getCopyPath() + " revision "
	                                    + entryPath.getCopyRevision() + ")" : ""));
	                    count++;
	                }
	            }
	            project.addRevision(logEntry.getAuthor(), ""+logEntry.getRevision(), logEntry.getDate(), logEntry.getMessage(),paths);
	        }
	        project.setName(projects[project_counter]);
	        revisions.addProject(project);
	        }
        	ByteArrayOutputStream baos=new ByteArrayOutputStream();
        	ByteArrayOutputStream baosHtml=new ByteArrayOutputStream();
        	JAXBContext context = JAXBContext.newInstance(Revisions.class);
        	Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(revisions, baos);
//			m.marshal(revisions, System.out);
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer =tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(SVNCommitHistoryTask.class.getResourceAsStream("svncommit.xsl")));
			transformer.transform(new javax.xml.transform.stream.StreamSource(new ByteArrayInputStream(baos.toByteArray())),new javax.xml.transform.stream.StreamResult(baosHtml));
			htmlOutput=baosHtml.toString();
			status=true;
		}
        catch (SVNException svne) {
            LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(svne));
		} catch (PropertyException e) {
			LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
		} catch (JAXBException e) {
			LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
		} catch(Exception e){
			LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
		}
		return status;
	}
	private static void setupLibrary() {
        /*
         * For using over http:// and https://
         */
        DAVRepositoryFactory.setup();
        /*
         * For using over svn:// and svn+xxx://
         */
        SVNRepositoryFactoryImpl.setup();
        /*
         * For using over file:///
         */
        FSRepositoryFactory.setup();
    }
    
    @XmlRootElement
    static class Revisions{
    	private List<Project> project;
    	public Revisions() {
    		project=new ArrayList<Project>();
		}
		public void setProject(List<Project> project) {
			this.project = project;
		}

		public List<Project> getProject() {
			return project;
		}
		public void addProject(Project proj){
			this.project.add(proj);
		}
    }
    
    static class Project {
    	private String name;
    	private List<Revision> revision;
    	@XmlTransient
    	private SimpleDateFormat sdf;
    	public Project() {
    		revision=new ArrayList<Revision>();
    		sdf=new SimpleDateFormat("dd-MMMM hh:mm a");
    	}
    	public void addRevision(String user,String revisionNumber,Date date,String comment,String []paths) {
    		Revision revision = new Revision();
    		revision.setUsername(user);
    		Commit commit=new Commit();
    		commit.setComment(comment);
    		commit.setDate(sdf.format(date));
    		commit.setAffectedFiles(paths);
    		commit.setRevisionNumber(revisionNumber);
    		revision.addCommit(commit);
    		if(this.revision.contains(revision)){
    			Revision existingRevision=this.revision.get(this.revision.indexOf(revision));
    			existingRevision.addCommit(commit);
    		}else{
    			this.revision.add(revision);
    		}
    	}
    	public void setRevision(List<Revision> revision) {
    		this.revision = revision;
    	}
    	public List<Revision> getRevision() {
    		return revision;
    	}
		public void setName(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
    }
    static class Revision {
//    	@XmlAttribute
    	private String username;
    	private List<Commit> commit;
    	public Revision() {
    		this.commit=new ArrayList<Commit>();
    	}
    	public String getUsername() {
    		return username;
    	}
    	public void setUsername(String username) {
    		this.username = username;
    	}
    	public List<Commit> getCommit() {
    		return commit;
    	}
    	public void addCommit(Commit commit) {
    		this.commit.add(commit);
    	}
    	public void setCommit(List<Commit> commit) {
    		this.commit = commit;
    	}
    	@Override
    	public boolean equals(Object obj) {
    		if(!(obj instanceof Revision)){
    			return false;
    		}
    		return username.equals(((Revision)obj).getUsername());
    	}
    }

    static class Commit {
      private String revisionNumber;
      private String date;
      private String comment;
      private String []affectedFiles;
    public String getRevisionNumber() {
    	return revisionNumber;
    }
    public void setRevisionNumber(String revisionNumber) {
    	this.revisionNumber = revisionNumber;
    }
    public String getComment() {
    	return comment;
    }
    public void setComment(String comment) {
    	this.comment = comment;
    }
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public void setAffectedFiles(String [] affectedFiles) {
		this.affectedFiles = affectedFiles;
	}
	public String [] getAffectedFiles() {
		return affectedFiles;
	}
    
    }
}