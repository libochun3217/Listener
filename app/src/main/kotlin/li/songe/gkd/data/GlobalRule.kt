package li.songe.gkd.data

import li.songe.gkd.a11y.launcherAppId

data class GlobalApp(
    val id: String,
    val enable: Boolean,
    val activityIds: List<String>,
    val excludeActivityIds: List<String>,
)

class GlobalRule(
    rule: RawSubscription.RawGlobalRule,
    g: ResolvedGlobalGroup,
) : ResolvedRule(
    rule = rule,
    g = g,
) {
    val groupExcludeAppIds = g.groupExcludeAppIds
    val group = g.group
    private val matchAnyApp = rule.matchAnyApp ?: group.matchAnyApp ?: true
    private val matchLauncher = rule.matchLauncher ?: group.matchLauncher ?: false
    private val matchSystemApp = rule.matchSystemApp ?: group.matchSystemApp ?: false

    override val type = "global"


    /**
     * 内置禁用>用户配置>规则自带
     * 范围越精确优先级越高
     */
    override fun matchActivity(appId: String, activityId: String?): Boolean {

        // 用户自定义禁用
        if (excludeData.excludeAppIds.contains(appId)) {
            return false
        }
        if (activityId != null && excludeData.activityIds.contains(appId to activityId)) {
            return false
        }
        return false
    }
}
