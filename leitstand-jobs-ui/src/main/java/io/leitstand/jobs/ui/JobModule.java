package io.leitstand.jobs.ui;

import static io.leitstand.ui.model.ModuleDescriptor.readModuleDescriptor;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.logging.Level.FINE;

import java.io.IOException;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import io.leitstand.ui.model.ModuleDescriptor;

@Dependent
public class JobModule {
	
	private static final Logger LOG = Logger.getLogger(JobModule.class.getName());

	@Produces
	public ModuleDescriptor getModuleDescriptor() {
		try {
			return readModuleDescriptor(currentThread()
										.getContextClassLoader()
										.getResource("/META-INF/resources/ui/modules/job/module.yaml"))
										.build();
		} catch (IOException e) {
			LOG.warning(() -> format("Cannot read image module. Reason: %s",e.getMessage()));
			LOG.log(FINE,e.getMessage(),e);
			throw new IllegalStateException(e);
		}
	}
	
}
