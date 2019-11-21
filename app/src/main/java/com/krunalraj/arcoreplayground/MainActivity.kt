package com.krunalraj.arcoreplayground

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import com.google.ar.sceneform.ux.ArFragment

import android.util.Log

import android.view.View
import com.google.ar.core.TrackingState
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import android.widget.LinearLayout
import android.net.Uri
import android.widget.ImageView
import PointerDrawable
import ModelLoader
import java.lang.ref.WeakReference
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode

import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity() {
    private var fragment: ArFragment? = null
    private var pointer = PointerDrawable()
    private var isTracking: Boolean = false
    private var isHitting: Boolean = false
    private var modelLoader: ModelLoader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        fragment =  supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment
        fragment!!.arSceneView.scene.addOnUpdateListener { frameTime ->
            fragment!!.onUpdate(frameTime)}
            onUpdate()
            initializeGallery()


        modelLoader = ModelLoader(WeakReference(this))
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

    }
    private fun onUpdate() {
        var trackingChanged = updateTracking()
        var contentView = findViewById<View>(android.R.id.content)
        if (trackingChanged) {
            if (isTracking) {
                contentView.overlay.add(pointer)
            } else {
                contentView.overlay.remove(pointer)
            }
            contentView.invalidate()
        }
        if (isTracking)
        {
            var hitTestChanged = updateHitTest()
            if (hitTestChanged)
            {
                pointer.setEnabled(isHitting)
                contentView.invalidate()
            }
        }
    }
    private fun updateTracking(): Boolean {
        val frame = fragment?.getArSceneView()?.arFrame
        val wasTracking = isTracking
        isTracking = frame != null && frame.camera.trackingState === TrackingState.TRACKING
        return isTracking != wasTracking
    }
    private fun updateHitTest(): Boolean {
        val frame = fragment?.arSceneView?.arFrame
        var pt = getScreenCenter()
        var hits: List<HitResult>
        var wasHitting = isHitting
        isHitting = false
        if (frame != null) {
            hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && (trackable as Plane).isPoseInPolygon(hit.hitPose)) {
                    isHitting = true
                    break
                }
            }
        }
        return wasHitting != isHitting
    }

    private fun getScreenCenter(): android.graphics.Point {
        var vw = findViewById<View>(android.R.id.content)
        Log.i("Point Initiated","Yes it did")
        return android.graphics.Point(vw.width / 2, vw.height / 2)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun initializeGallery() {
        var gallery = findViewById<LinearLayout>(R.id.gallery_layout)

        val andy = ImageView(this)
        andy.setImageResource(R.drawable.droid_thumb)
        andy.setContentDescription("andy")
        andy.setOnClickListener({ view -> addObject(Uri.parse("andy_dance.sfb")) })
        gallery.addView(andy)

        val cabin = ImageView(this)
        cabin.setImageResource(R.drawable.cabin_thumb)
        cabin.setContentDescription("cabin")
        cabin.setOnClickListener({ view -> addObject(Uri.parse("Cabin.sfb")) })
        gallery.addView(cabin)

        val house = ImageView(this)
        house.setImageResource(R.drawable.house_thumb)
        house.setContentDescription("house")
        house.setOnClickListener({ view -> addObject(Uri.parse("House.sfb")) })
        gallery.addView(house)

        val igloo = ImageView(this)
        igloo.setImageResource(R.drawable.igloo_thumb)
        igloo.setContentDescription("igloo")
        igloo.setOnClickListener({ view -> addObject(Uri.parse("igloo.sfb")) })
        gallery.addView(igloo)
    }
    private fun addObject(model: Uri) {
        val frame = fragment?.getArSceneView()?.arFrame
        val pt = getScreenCenter()
        val hits: List<HitResult>
        if (frame != null) {
            hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    modelLoader?.loadModel(hit.createAnchor(), model)
                    break

                }
            }
        }
    }
    fun addNodeToScene(anchor: Anchor, renderable: ModelRenderable) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment?.getTransformationSystem())
        node.renderable = renderable
        node.setParent(anchorNode)
        fragment?.getArSceneView()?.scene?.addChild(anchorNode)
        node.select()
    }
    fun onException(throwable: Throwable) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(throwable.message)
            .setTitle("Codelab error!")
        val dialog = builder.create()
        dialog.show()
        return
    }
}
