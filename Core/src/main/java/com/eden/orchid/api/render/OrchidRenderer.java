package com.eden.orchid.api.render;

import com.eden.orchid.api.resources.OrchidPage;
import com.eden.orchid.api.resources.OrchidResources;
import com.eden.orchid.api.resources.resource.OrchidResource;
import org.apache.commons.io.FilenameUtils;

import javax.inject.Inject;

public abstract class OrchidRenderer {

    protected OrchidResources resources;

    @Inject
    public OrchidRenderer(OrchidResources resources) {
        this.resources = resources;
    }

    /**
     * Render the given page with the given template resource. A list of templates can be given, and the first resource
     * that could be located will be the template used.
     *
     * @param page the page to render
     * @param templates a list of templates to attempt to render with
     * @return true if the page was successfully rendered, false otherwise
     */
    public final boolean renderTemplate(OrchidPage page, String... templates) {
        for(String template : templates) {
            OrchidResource templateResource = resources.getResourceEntry(template);

            if (templateResource != null) {
                return render(page, FilenameUtils.getExtension(template), templateResource.getContent());
            }
        }

        return false;
    }

    /**
     * Render the given page using a literal String as a template.
     *
     * @param page the page to render
     * @param extension the extension that the content represents and should be compiled against
     * @param content the template string to render
     * @return true if the page was successfully rendered, false otherwise
     */
    public final boolean renderString(OrchidPage page, String extension, String content) {
        return render(page, extension, content);
    }

    /**
     * Render the content of a page directly, without any template.
     *
     * @param page the page to render
     * @return true if the page was successfully rendered, false otherwise
     */
    public final boolean renderRaw(OrchidPage page) {
        return render(page, page.getResource().getReference().getExtension(), page.getResource().getContent());
    }

    /**
     * Internal representation of a 'render' operation.
     *
     * @param page the page to render
     * @param extension the extension that the content represents and should be compiled against
     * @param content the template string to render
     * @return true if the page was successfully rendered, false otherwise
     */
    protected abstract boolean render(OrchidPage page, String extension, String content);
}