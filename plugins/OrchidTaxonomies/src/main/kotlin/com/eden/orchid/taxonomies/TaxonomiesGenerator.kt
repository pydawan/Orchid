package com.eden.orchid.taxonomies

import com.eden.orchid.api.OrchidContext
import com.eden.orchid.api.generators.OrchidCollection
import com.eden.orchid.api.generators.OrchidGenerator
import com.eden.orchid.api.options.annotations.Description
import com.eden.orchid.api.options.annotations.Option
import com.eden.orchid.api.resources.resource.StringResource
import com.eden.orchid.api.theme.pages.OrchidPage
import com.eden.orchid.api.theme.pages.OrchidReference
import com.eden.orchid.api.theme.permalinks.PermalinkStrategy
import com.eden.orchid.taxonomies.collections.TaxonomyCollection
import com.eden.orchid.taxonomies.models.TaxonomiesModel
import com.eden.orchid.taxonomies.models.Taxonomy
import com.eden.orchid.taxonomies.models.Term
import com.eden.orchid.taxonomies.pages.TaxonomyArchivePage
import com.eden.orchid.taxonomies.pages.TermArchivePage
import com.eden.orchid.taxonomies.utils.getSingleTermValue
import com.eden.orchid.taxonomies.utils.getTermValues
import org.json.JSONArray
import org.json.JSONObject
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaxonomiesGenerator @Inject
constructor(context: OrchidContext, val model: TaxonomiesModel, val permalinkStrategy: PermalinkStrategy) : OrchidGenerator(context, GENERATOR_KEY, OrchidGenerator.PRIORITY_DEFAULT) {

    companion object {
        const val GENERATOR_KEY = "taxonomies"
    }

    @Option
    @Description("An array of Taxonomy configurations.")
    lateinit var taxonomies: JSONArray

    override fun startIndexing(): List<OrchidPage> {
        model.initialize()

        if (taxonomies.length() > 0) {
            for (i in 0 until taxonomies.length()) {
                val taxonomy = taxonomies.get(i)
                val taxonomyKey: String
                val taxonomyOptions: JSONObject?

                if(taxonomy is String) {
                    taxonomyKey = taxonomy
                    taxonomyOptions = null
                }
                else if(taxonomy is JSONObject) {
                    if (taxonomy.length() == 1) {
                        taxonomyKey = taxonomy.keySet().first()
                        taxonomyOptions = taxonomy.get(taxonomyKey) as? JSONObject
                    }
                    else {
                        taxonomyKey = taxonomy.getString("key")
                        taxonomyOptions = taxonomy
                    }
                }
                else {
                    continue
                }

                val taxonomyModel = model.getTaxonomy(taxonomyKey, taxonomyOptions ?: JSONObject())
                val enabledGeneratorKeys = context.getGeneratorKeys(taxonomyModel.includeFrom, taxonomyModel.excludeFrom)

                context.internalIndex.getGeneratorPages(enabledGeneratorKeys).forEach { page ->
                    val pageTerms = HashSet<String?>()
                    if(taxonomyModel.single) {
                        pageTerms.add(page.getSingleTermValue(taxonomyKey))
                    }
                    else {
                        if(taxonomyModel.singleKey.isNotBlank()) {
                            pageTerms.add(page.getSingleTermValue(taxonomyModel.singleKey))
                        }

                        pageTerms.addAll(page.getTermValues(taxonomyKey))
                    }

                    pageTerms.forEach { term ->
                        if(term != null) {
                            model.addPage(taxonomyKey, term, page)
                        }
                    }
                }
            }
        }

        model.onIndexingTermsFinished()

        return buildAllTaxonomiesPages()
    }

    override fun startGeneration(pages: Stream<out OrchidPage>) {
        pages.forEach { context.renderTemplate(it) }
    }

    override fun getCollections(): MutableList<out OrchidCollection<*>> {
        val collections = ArrayList<OrchidCollection<*>>()

        model.taxonomies.values.forEach { taxonomy ->
            collections.add(TaxonomyCollection(this, taxonomy))
        }

        return collections
    }

// Archive Page Helpers
//----------------------------------------------------------------------------------------------------------------------

    // build all pages for each taxonomy
    private fun buildAllTaxonomiesPages(): List<OrchidPage> {
        val archivePages = ArrayList<OrchidPage>()

        model.taxonomies.values.forEach { taxonomy ->
            taxonomy.allTerms.forEach { term ->
                archivePages.addAll(buildTermArchivePages(taxonomy, term))
            }
            archivePages.addAll(buildTaxonomyLandingPages(taxonomy))
        }

        return archivePages
    }

    // build a set of pages that display all the terms in a given taxonomy
    private fun buildTaxonomyLandingPages(taxonomy: Taxonomy): List<OrchidPage> {
        val terms = taxonomy.allTerms
        val termPages = ArrayList<OrchidPage>()

        val pages = Math.ceil((taxonomy.terms.size / taxonomy.pageSize).toDouble()).toInt()

        for (i in 0..pages) {
            val termList = terms.subList(i * taxonomy.pageSize, Math.min((i + 1) * taxonomy.pageSize, terms.size))
            if (termList.isNotEmpty()) {
                var title = taxonomy.title
                if (i != 0) title += " (Page ${i + 1})"

                val pageRef = OrchidReference(context, "taxonomy.html")
                pageRef.title = title

                val page = TaxonomyArchivePage(StringResource("", pageRef), model, taxonomy, i + 1)

                permalinkStrategy.applyPermalink(page, page.taxonomy.permalink)

                termPages.add(page)
            }
        }

        linkPages(termPages)
        taxonomy.archivePages = termPages

        return termPages
    }

    // build a set of pages that display all the items in a given term within a taxonomy
    private fun buildTermArchivePages(taxonomy: Taxonomy, term: Term): List<OrchidPage> {
        val pagesList = term.allPages
        val termArchivePages = ArrayList<OrchidPage>()

        val pages = Math.ceil((pagesList.size / term.pageSize).toDouble()).toInt()

        for (i in 0..pages) {
            val termPageList = pagesList.subList(i * term.pageSize, Math.min((i + 1) * term.pageSize, pagesList.size))
            if (termPageList.isNotEmpty()) {
                var title = term.title
                if (i != 0) title += " (Page ${i + 1})"

                val pageRef = OrchidReference(context, "term.html")
                pageRef.title = title

                val page = TermArchivePage(StringResource("", pageRef), model, termPageList, taxonomy, term, i + 1)
                permalinkStrategy.applyPermalink(page, page.term.permalink)
                termArchivePages.add(page)
            }
        }

        linkPages(termArchivePages)
        term.archivePages = termArchivePages

        return termArchivePages
    }

// Other Utils
//----------------------------------------------------------------------------------------------------------------------

    private fun linkPages(pages: List<OrchidPage>) {
        var i = 0
        for (post in pages) {
            if (next(pages, i) != null) {
                post.next = next(pages, i)
            }
            if (previous(pages, i) != null) {
                post.previous = previous(pages, i)
            }
            i++
        }
    }

    private fun previous(pages: List<OrchidPage>, i: Int): OrchidPage? {
        if (pages.size > 1) {
            if (i != 0) {
                return pages[i - 1]
            }
        }

        return null
    }

    private fun next(pages: List<OrchidPage>, i: Int): OrchidPage? {
        if (pages.size > 1) {
            if (i < pages.size - 1) {
                return pages[i + 1]
            }
        }

        return null
    }

}
