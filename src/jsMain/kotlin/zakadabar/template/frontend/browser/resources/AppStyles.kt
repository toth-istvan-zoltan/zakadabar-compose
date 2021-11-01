/*
 * @copyright@
 */
package zakadabar.template.frontend.browser.resources

import zakadabar.core.resource.css.ZkCssStyleSheet
import zakadabar.core.resource.css.cssStyleSheet
import zakadabar.core.resource.css.px

val appStyles by cssStyleSheet(AppStyles())

class AppStyles : ZkCssStyleSheet() {

    // -------------------------------------------------------------------------
    // CSS styles the application
    // -------------------------------------------------------------------------

    val home by cssClass {
        overflowY = "scroll"
        padding = 20.px
    }

}