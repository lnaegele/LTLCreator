package editor.validator.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import util.fsmmodel.Fsm;
import util.fsmmodel.State;
import editor.validator.ConstraintValidator;
import editor.validator.ValidationCanceledException;

public class NuSMVModelCheckerWrapper extends ConstraintValidator {
	
	private boolean askForNuSmv = true;
//	private String nuSmvFile = "C:\\Users\\ludwig\\Documents\\TUM\\12 SS\\Thesis\\NuSMV\\bin\\NuSMV.exe";
	private String nuSmvFile = null;
	private Map<String, String> replacementNames = new HashMap<String, String>();
	
	public NuSMVModelCheckerWrapper() {
		try {
			this.nuSmvFile = new File(ClassLoader.getSystemClassLoader().getResource("./NuSMV/NuSMV.exe").toURI()).getPath();
			System.out.println(this.nuSmvFile);
			try {
				this.nuSmvFile = URLDecoder.decode(this.nuSmvFile, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			System.out.println(this.nuSmvFile);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}
	
	@Override
	public String getLTLForVariable(String variable) {
		Fsm model = super.getModel();
		if (model==null) return null;
		
		boolean found = false;
		for (State s : model.getAllStates()) {
			if (s.getId().equals(variable)) {
				found = true;
				break;
			}
		}
		if (!found) return null;
		
		if (!this.replacementNames.containsKey(variable)) {
			if (variable=="ANY") this.replacementNames.put(variable, "TRUE");
			else this.replacementNames.put(variable, ""+(this.replacementNames.size()+1));
		}
		return "state = " + this.replacementNames.get(variable);
	}
	
	@Override
	public boolean validate(String ltlFormula) throws ValidationCanceledException {
		while (true) {
			String nuSmvFile = getNuSmvFile();
			if (nuSmvFile==null) throw new ValidationCanceledException();
			
			Fsm model = super.getModel();

			File file = null;
			try {
				// Create tmp names for all states
				if (model!=null) {
					for (State state : model.getAllStates()) getLTLForVariable(state.getId());
				}

				String diagram = createNuSMVDiagram(ltlFormula, model, this.replacementNames);
				file = writeToFile(diagram);
				
				List<String> consoleOutput = runShell(file.getAbsolutePath(), nuSmvFile);
				boolean result = evaluateResult(consoleOutput);
				
				return result;
			} catch (Exception e) {
				throw new ValidationCanceledException();
			} finally {
				if (file!=null) file.delete();
			}
		}
	}
	
	/**
	 * Returns the path to the NuSMV.exe file. If not already set, the user may
	 * be asked whether he wants to give the path of the model checker.
	 * 
	 * @return
	 */
	private String getNuSmvFile() {		
		if (this.nuSmvFile!=null && new File(this.nuSmvFile).isFile() && new File(this.nuSmvFile).exists()) {
			return this.nuSmvFile;
		}
		
		String result = null;
		if (this.askForNuSmv) {
			int r = JOptionPane.showConfirmDialog(null, "Do you want to connect a NuSMV model checker so that constraints can be directly checked?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (r==JOptionPane.YES_OPTION) {
//				File tmp = new File("LTLCreator.tmp");
//				String lastNuSmvFile = null;
//					if (tmp.isFile()) {
//					try {
//						BufferedReader br = new BufferedReader(new FileReader(tmp));
//						String s = br.readLine();
//						br.close();
//						if (s!=null && new File(s).isFile()) {
//							
//						}
//					} catch (Exception e) {}
//				}
				
				JFileChooser fc = new JFileChooser(this.nuSmvFile==null ? null : new File(this.nuSmvFile));
				fc.setFileFilter(new FileFilter() {
					@Override
					public String getDescription() {
						return "NuSMV.exe";
					}
					@Override
					public boolean accept(File f) {
						if (f.isDirectory()) {
					        return true;
					    }
					    return f.getName().equals("NuSMV.exe");
					}
				});
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setMultiSelectionEnabled(false);
				if (fc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
					this.nuSmvFile = fc.getSelectedFile().getAbsolutePath();
					result = this.nuSmvFile;
				}
				else this.askForNuSmv = false;
			}
			else this.askForNuSmv = false;
		}
		return result;
	}
	
	/**
	 * Converts a state machine and a ltl formula to a NuSMV diagramm.
	 * 
	 * @param ltlFormula the formula to be checked.
	 * @param model the state machine behavior.
	 * @param replacementNames the replacements for state names.
	 * @return
	 */
	private static String createNuSMVDiagram(String ltlFormula, Fsm model, Map<String, String> replacementNames) {
		StringBuffer sb = new StringBuffer();
		sb.append("MODULE main\n");
		
		if (model==null) {
			sb.append("  VAR\n");
			sb.append("    dummy: 1..1;\n");
			sb.append("  ASSIGN\n");
			sb.append("    init(dummy) := 1\n");
			sb.append("    next(dummy) := 1;");
		}
		else {
			sb.append("  VAR\n");
			sb.append("    state: 1.." + replacementNames.size() +";\n");
			sb.append("  ASSIGN\n");
			sb.append("    init(state) := " + replacementNames.get(model.getInitialState().getId()) + ";\n");
			sb.append("    next(state) := case ");
	
			for (State state : model.getAllStates()) {
				sb.append("state = ").append(replacementNames.get(state.getId())).append(" : ");
				
				List<State> targets = state.getTargets();
				if (targets.size()==0) sb.append(replacementNames.get(state.getId()));
				else if (targets.size()==1) sb.append(replacementNames.get(targets.get(0).getId()));
				else {
					sb.append("{");
					sb.append(replacementNames.get(targets.get(0).getId()));
					for (int t=1; t<targets.size(); t++) {
						sb.append(", ");
						sb.append(replacementNames.get(targets.get(t).getId()));
					}
					sb.append("}");
				}
				sb.append(";\n                        ");
			}
			sb.append("TRUE : state;\n");
			sb.append("                   esac;");
		}
		
		sb.append("\n\n");
		sb.append("LTLSPEC ").append(ltlFormula);
		
		return sb.toString();
	}
	
	/**
	 * Writes a string to file.
	 * 
	 * @param content
	 * @return
	 * @throws IOException
	 */
	private static File writeToFile(String content) throws IOException {
		File temp = File.createTempFile("rsmc",".smv");
		temp.deleteOnExit();
		
		FileWriter fw = new FileWriter(temp);
		fw.write(content);
		fw.flush();
		fw.close();
		return temp;
	}
	
	/**
	 * Executes the model checker in a shell. This function will block until the
	 * checking process is finished.
	 * 
	 * @param file the NuSMV diagram.
	 * @param nuSmvFile path to NuSMV.exe.
	 * @return result of the validation.
	 * @throws Exception
	 */
	private static List<String> runShell(String file, String nuSmvFile) throws Exception {
		ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", nuSmvFile, file);
	    processBuilder.directory(new File(System.getProperty("java.io.tmpdir")));
	 
	    final List<String> result = new ArrayList<String>();
		final Process process = processBuilder.start();
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(new Runnable() {
			public void run() {
				Scanner scanner = new Scanner(process.getInputStream());
				while (scanner.hasNextLine()) {
					String l = scanner.nextLine();
					result.add(l);
				}
				scanner.close();
			}
		});
	 
	    process.waitFor();// anscheinend wird hier nicht immer richtig gewartet!!!
	    
	    executorService.shutdown();
	    while (!executorService.isTerminated()) {
	    	try {
	    		executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	    	} catch (InterruptedException e) {}
	    }
	    return result;
	}
	
	/**
	 * Tries to find te outcome of a validation.
	 */
	private static boolean evaluateResult(List<String> outputLines) {
		for (String line : outputLines) {
			if (line.startsWith("-- ")) {
				if (line.endsWith("is true"))
					return true;
				else if (line.endsWith("is false"))
					return false;
			}
		}
		throw new RuntimeException();
	}
	
}
