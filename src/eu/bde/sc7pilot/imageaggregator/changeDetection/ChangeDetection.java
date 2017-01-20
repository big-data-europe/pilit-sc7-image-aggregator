package eu.bde.sc7pilot.imageaggregator.changeDetection;

import java.util.List;

import eu.bde.sc7pilot.imageaggregator.model.Change;
import eu.bde.sc7pilot.imageaggregator.model.Image;
import eu.bde.sc7pilot.imageaggregator.model.ImageData;

public interface ChangeDetection {
	
	public List<Change> detectChanges(List<Image> images, ImageData imageData, String finalOutput) throws Exception;
}
