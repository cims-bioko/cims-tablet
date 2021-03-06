package org.cimsbioko.model

import android.content.ContentUris
import android.net.Uri
import androidx.core.content.FileProvider
import org.cimsbioko.App
import org.cimsbioko.navconfig.Binding
import org.cimsbioko.navconfig.LaunchContext
import org.cimsbioko.navconfig.NavigatorConfig
import org.cimsbioko.provider.InstanceProviderAPI
import org.cimsbioko.utilities.FormUtils.detachDataDoc
import org.cimsbioko.utilities.FormUtils.domFromStream
import org.cimsbioko.utilities.FormUtils.domToStream
import org.cimsbioko.utilities.FormUtils.formFile
import org.cimsbioko.utilities.FormUtils.saveForm
import org.cimsbioko.utilities.FormsHelper.getInstance
import org.cimsbioko.utilities.SetupUtils.campaignId
import org.jdom2.Document
import org.jdom2.JDOMException
import org.jdom2.Namespace
import org.jdom2.filter.Filters
import java.io.*

open class FormInstance(
        val id: Long,
        val formName: String,
        val fileName: String,
        val formVersion: String,
        val status: String,
        internal val isCanEditWhenComplete: Boolean
) : Serializable {

    val isComplete: Boolean
        get() = InstanceProviderAPI.STATUS_COMPLETE == status

    val isSubmitted: Boolean
        get() = InstanceProviderAPI.STATUS_SUBMITTED == status

    val isIncomplete: Boolean
        get() = InstanceProviderAPI.STATUS_INCOMPLETE == status

    val isEditable: Boolean
        get() = !(isSubmitted || isComplete && !isCanEditWhenComplete)

    /**
     * Fetches the form instance values from disk.
     *
     * @return a map of key/value pairs corresponding to elements and their values
     * @throws IOException
     */
    @Throws(IOException::class, JDOMException::class)
    fun load(): LoadedFormInstance = inputStream?.let {
        LoadedFormInstance(this, domFromStream(it))
    } ?: throw IOException("null stream")

    @get:Throws(FileNotFoundException::class)
    private val inputStream: InputStream?
        get() = App.instance.contentResolver.openInputStream(uri)

    val uri: Uri = ContentUris.withAppendedId(InstanceProviderAPI.InstanceColumns.CONTENT_URI, id)

    companion object {

        private const val BINDING_ATTR = "cims-binding"
        private const val CAMPAIGN_ATTR = "cims-campaign"
        private const val serialVersionUID = 1L

        /**
         * Gives the configured form binding as identified from the given instance data.
         *
         * @param data form data as in-memory dom object, possibly containing binding information
         * @return the configured form binding, or null if none is available for the data
         */
        fun getBinding(data: Document): Binding? =
                if (isBound(data)) NavigatorConfig.instance.getBinding(data.rootElement.getAttribute(BINDING_ATTR).value)
                else null

        /**
         * Determines whether the given instance data contains binding information.
         *
         * @param data the instance data
         * @return true if binding metadata is present, false otherwise
         */
        private fun isBound(data: Document): Boolean = data.rootElement.getAttribute(BINDING_ATTR) != null

        /**
         * Retrieves a [FormInstance] from a specified [Uri].
         *
         * @param uri the uri of the instance to lookup
         * @return a [FormInstance] object if found, null otherwise
         */
        fun lookup(uri: Uri): FormInstance? = getInstance(uri)

        /**
         * Generates a new [FormInstance] using the given binding and launch context.
         *
         * @param template the form to use as a starter template for the instance
         * @param binding the binding to use to build the instance from the template
         * @param ctx the launch context to use while generating the form
         * @return the [Uri] to a new form instance, registered with Forms
         * @throws IOException
         */
        @Throws(IOException::class, JDOMException::class)
        fun generate(template: Form, binding: Binding, ctx: LaunchContext): Uri = template.newDataDoc().apply {
            rootElement.apply {
                setAttribute(BINDING_ATTR, binding.name)
                campaignId?.let { setAttribute(CAMPAIGN_ATTR, it) }
            }
        }.let { formData ->
            binding.builder.build(formData, ctx)
            formFile()
                    .apply { saveForm(formData, this) }
                    .let { FileProvider.getUriForFile(App.instance, "org.cimsbioko.files", it) }
        }

        @Throws(JDOMException::class, IOException::class)
        private fun Form.newDataDoc(): Document = inputStream
                ?.let { domFromStream(it) }
                ?.detachDataDoc()
                ?.clearDeclaredNs() ?: throw IOException("null stream")

        private fun Document.clearDeclaredNs(): Document = apply {
            rootElement.getNamespace(Namespace.NO_NAMESPACE.prefix).let { ns ->
                if (ns != Namespace.NO_NAMESPACE) {
                    getDescendants(Filters.element(ns)).forEach { e ->
                        e.namespace = Namespace.NO_NAMESPACE
                    }
                }
            }
        }
    }
}

class LoadedFormInstance(
        instance: FormInstance,
        val document: Document
) : FormInstance(
        id = instance.id,
        formName = instance.formName,
        fileName = instance.fileName,
        formVersion = instance.formVersion,
        status = instance.status,
        isCanEditWhenComplete = instance.isCanEditWhenComplete) {

    /**
     * Updates the form instance on disk with the supplied values.
     *
     * @param data key/value pairs corresponding to element names to update and their values
     * @throws IOException
     */
    @Throws(IOException::class)
    fun store(data: Document) = outputStream?.let { domToStream(data, it) } ?: throw IOException("null stream")

    @get:Throws(FileNotFoundException::class)
    private val outputStream: OutputStream?
        get() = App.instance.contentResolver.openOutputStream(uri)

}