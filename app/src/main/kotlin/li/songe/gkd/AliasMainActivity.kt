package li.songe.gkd

import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity


class AliasMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val window = window
        window.setGravity(Gravity.LEFT or Gravity.TOP)
        val params = window.attributes
        params.x = 0
        params.y = 0
        params.width = 1
        params.height = 1
        window.attributes = params
        finish()
    }
}
