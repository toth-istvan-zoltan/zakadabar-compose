/*
 * @copyright@
 */

package zakadabar.template.backend

import zakadabar.core.authorize.AppRolesBase
import zakadabar.core.authorize.SimpleRoleAuthorizerProvider
import zakadabar.core.route.RoutedModule
import zakadabar.core.server.server
import zakadabar.core.util.PublicApi
import zakadabar.template.backend.business.ExampleEntityBl

@PublicApi
object Module : RoutedModule {

    override fun onModuleLoad() {
        zakadabar.lib.accounts.install(MyRoles)
        zakadabar.lib.i18n.install()

        server += SimpleRoleAuthorizerProvider {
            all = MyRoles.siteMember
            read = MyRoles.myRole
        }

        server += ExampleEntityBl()
    }

}

object MyRoles : AppRolesBase() {
    val myRole by "my-role"
    val siteMember by "site-member"
}