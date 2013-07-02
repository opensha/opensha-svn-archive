package scratch.kevin.simulators;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

import com.google.common.collect.Lists;

public class EigenDecomp {
	
	public static void main(String[] args) throws IOException {
		File dir = new File("/home/kevin/Simulators");
		File geomFile = new File(dir, "ALLCAL2_1-7-11_Geometry.dat");
		System.out.println("Loading geometry...");
		General_EQSIM_Tools tools = new General_EQSIM_Tools(geomFile);
//		File eventFile = new File(dir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.barall");
		File eventFile = new File(dir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.long.barall");
		System.out.println("Loading events...");
		tools.read_EQSIMv04_EventsFile(eventFile);
		List<EQSIM_Event> events = tools.getEventsList();
		
		List<RuptureIdentifier> rupIdens = Lists.newArrayList();
		List<String> rupIdenNames = Lists.newArrayList();
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAF_CHOLAME_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("SAF Cholame 7+");
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAF_CARRIZO_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("SAF Carrizo 7+");
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.GARLOCK_WEST_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("Garlock 7+");
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAF_MOJAVE_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("SAF Mojave 7+");
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAF_COACHELLA_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("SAF Coachella 7+");
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAN_JACINTO__ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("San Jacinto 7+");
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.ELSINORE_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("Elsinore 7+");
		
		List<List<EQSIM_Event>> matchesLists = Lists.newArrayList();
		
		for (RuptureIdentifier rupIden : rupIdens) 
			matchesLists.add(rupIden.getMatches(events));
		
		List<Double> medianRPs = Lists.newArrayList();
		for (List<EQSIM_Event> matches : matchesLists) {
			double[] rps = new double[matches.size()-1];
			for (int i=1; i<matches.size(); i++)
				rps[i-1] = matches.get(i).getTimeInYears()-matches.get(i-1).getTimeInYears();
			medianRPs.add(BatchPlotGen.median(rps));
		}
		
		RealMatrix correlMatrix = new Array2DRowRealMatrix(rupIdens.size(), rupIdens.size());
		
		double fractOfRPToLook = 0.025;
		boolean includeBackwards = false;
		double hardCodedWindowLength = 0;
		
		// correlation from r to c within numYears
		for (int r=0; r<rupIdens.size(); r++) {
			List<EQSIM_Event> rMatches = matchesLists.get(r);
			
			int numR = rMatches.size();
			for (int c=0; c<rupIdens.size(); c++) {
				List<EQSIM_Event> cMatches = matchesLists.get(c);
				
				double numYears = medianRPs.get(c)*fractOfRPToLook;
				if (hardCodedWindowLength > 0)
					numYears = hardCodedWindowLength;
				
				int numCFollowers = 0;
				
				for (EQSIM_Event rEvent : rMatches) {
					double rTime = rEvent.getTimeInYears();
					double windowStart;
					if (includeBackwards)
						windowStart = rTime-numYears;
					else
						windowStart = rTime;
					double windowEnd = rTime+numYears;
					for (EQSIM_Event cEvent : cMatches) {
						double cTime = cEvent.getTimeInYears();
						if (cTime < windowStart)
							continue;
						if (cTime <= windowEnd) {
							numCFollowers++;
							break;
						}
					}
				}
				
				double correlation = (double)numCFollowers / (double)numR;
//				double correlation = (double)numCFollowers / (0.5d*(numR+cMatches.size()));
				
				System.out.println("("+r+","+c+"): "+correlation);
				
				correlMatrix.setEntry(r, c, correlation);
			}
		}
		
		// make a crude plot
		displayMatrix("Correlation Matrix", correlMatrix);
		
//		boolean symmetric = true;
//		for (int r=0; r<correlMatrix.getRowDimension(); r++) {
//			for (int c=0; c<correlMatrix.getColumnDimension(); c++) {
//				if (r > c)
//					continue;
//				double v1 = correlMatrix.getEntry(r, c);
//				double v2 = correlMatrix.getEntry(c, r);
//				if (v1 != v2) {
//					symmetric = false;
//					break;
//				}
//			}
//		}
//		
//		System.out.println("Symmetric? "+symmetric);
//		
//		RealMatrix symmetricCorrelMatrix;
//		
//		if (symmetric) {
//			symmetricCorrelMatrix = correlMatrix;
//		} else {
//			System.out.println("Building max matrix for symmetry");
//			symmetricCorrelMatrix = new Array2DRowRealMatrix(rupIdens.size(), rupIdens.size());
//			for (int r=0; r<correlMatrix.getRowDimension(); r++) {
//				for (int c=0; c<correlMatrix.getColumnDimension(); c++) {
//					double v1 = correlMatrix.getEntry(r, c);
//					double v2 = correlMatrix.getEntry(c, r);
//					double max;
//					if (v1 > v2)
//						max = v1;
//					else
//						max = v2;
//					symmetricCorrelMatrix.setEntry(r, c, max);
//				}
//			}
//			displayMatrix("Symmetrix Max Correlation Matrix", symmetricCorrelMatrix);
//		}
		
		EigenDecomposition eig = new EigenDecomposition(correlMatrix);

		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();

		int numEig = eig.getImagEigenvalues().length;
		cpt = cpt.rescale(0d, numEig-1);

		for (int i=0; i<numEig; i++) {
			RealVector e = eig.getEigenvector(i);
			EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(0d, e.getMaxIndex()+1, 1d);
			for (int n=0; n<func.getNum(); n++)
				func.set(n, e.getEntry(n));
			func.setName("Eigenvector with Eigenvalue="+eig.getRealEigenvalue(i));
			funcs.add(func);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, cpt.getColor((float)i)));
		}

		new GraphWindow(funcs, "Eigenvectors", chars);
	}
	
	private static void displayMatrix(String title, RealMatrix matrix) throws IOException {
		int pixelsPerCell = 20;
		int imageSize = pixelsPerCell * matrix.getRowDimension();
		BufferedImage img = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_4BYTE_ABGR);
		
		CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
		cpt = cpt.rescale(0d, 1d);
		
		for (int x=0; x<img.getWidth(); x++) {
			for (int y=0; y<img.getHeight(); y++) {
				// need to flip y
				int myY = img.getHeight()-1-y;
				
				int r = x/pixelsPerCell;
				int c = myY/pixelsPerCell;
				
//				System.out.println("("+x+","+y+") => ("+r+","+c+")");
				
				Color color = cpt.getColor((float)matrix.getEntry(r, c));
				
				img.setRGB(x, y, color.getRGB());
			}
		}
		
		JFrame frame = new JFrame();
		frame.setTitle(title);
		JLabel lblimage = new JLabel(new ImageIcon(img));
		frame.getContentPane().add(lblimage, BorderLayout.CENTER);
		frame.setSize(img.getWidth()+50, img.getHeight()+50);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

}