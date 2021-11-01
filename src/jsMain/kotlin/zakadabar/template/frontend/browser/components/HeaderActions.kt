/*
 * @copyright@
 */
package zakadabar.template.frontend.browser.components

import zakadabar.core.browser.ZkElement
import zakadabar.core.browser.theme.ZkThemeRotate
import zakadabar.core.resource.ZkIcons
import zakadabar.template.frontend.browser.resources.AppDarkTheme
import zakadabar.template.frontend.browser.resources.AppLightTheme

class HeaderActions : ZkElement() {

    override fun onCreate() {
        + ZkThemeRotate(
            ZkIcons.darkMode to AppDarkTheme(),
            ZkIcons.lightMode to AppLightTheme()
        )
    }

}