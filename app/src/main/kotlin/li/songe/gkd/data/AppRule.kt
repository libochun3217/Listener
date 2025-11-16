package li.songe.gkd.data

class AppRule(
    rule: RawSubscription.RawAppRule,
    g: ResolvedAppGroup,
) : ResolvedRule(
    rule = rule,
    g = g,
) {
    val group = g.group
    val app = g.app
    val enable = true
    val appId = app.id
    private val activityIds = getFixActivityIds(app.id, rule.activityIds ?: group.activityIds)
    private val excludeActivityIds =
        (getFixActivityIds(
            app.id,
            rule.excludeActivityIds ?: group.excludeActivityIds
        ) + (excludeData.activityIds.filter { e -> e.first == appId }
            .map { e -> e.second })).distinct()

    override val type = "app"
    override fun matchActivity(appId: String, activityId: String?): Boolean {
        if (!enable) return false
        if (appId != app.id) return false
        activityId ?: return true
        if (excludeActivityIds.any { activityId.startsWith(it) }) return false
        return activityIds.isEmpty() || activityIds.any { activityId.startsWith(it) }
    }
}
