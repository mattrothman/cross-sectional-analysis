import ij.process.*;
import ij.*;

class Preprocessor {
    ImagePlus imp;
    ImageProcessor ip;

    public Preprocessor(ImagePlus imp, ImageProcessor ip) {
        this.imp = imp;
        this.ip = ip;
    }

    /**
     * Process 
     */
    public void process() {
        ip.setSnapshotCopyMode(true);
	 	ip.findEdges();
        ip.setSnapshotCopyMode(false);
    }

}
