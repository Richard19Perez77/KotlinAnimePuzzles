package com.compose.myapplication

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.compose.myapplication.databinding.FragmentFirstBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    companion object {
        const val PUZZLE_LOG = "puzzleLog"
        const val TAG = "com.updated.puzzles.PuzzleFragment"
    }

    lateinit var myMediaPlayer: MyMediaPlayer
    lateinit var mySoundPool: MySoundPool
    lateinit var sharedpreferences: SharedPreferences
    lateinit var noisyAudioStreamReceiver: NoisyAudioStreamReceiver
    var common = CommonVariables.getInstance()

    private val intentFilter = IntentFilter(
        AudioManager.ACTION_AUDIO_BECOMING_NOISY
    )

    private fun startPlayback() {
        if (common.isLogging) Log.d(PUZZLE_LOG, "startPlayback NoisyAudioStreamReceiver")
        activity?.registerReceiver(noisyAudioStreamReceiver, intentFilter)
    }

    private fun stopPlayback() {
        if (common.isLogging) Log.d(PUZZLE_LOG, "stopPlayback NoisyAudioStreamReceiver")
        activity?.unregisterReceiver(noisyAudioStreamReceiver)
    }

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /**
     * Start of receiver inner class to handle headphones becoming unplugged
     */
    inner class NoisyAudioStreamReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (common.isLogging) {
                Log.d(PUZZLE_LOG, "onReceive NoisyAudioStreamReceiver")
            }

            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                // quiet the media player
                // myMediaPlayer.setNewVolume(0.1f)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_puzzle, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.new_puzzle -> {
                binding.puzzle.newPuzzle()
                return true
            }

            R.id.menu_stats -> {
                startStatsFragment()
                return true
            }

            R.id.music_toggle -> {
                binding.puzzle.toggleMusic()
                return true
            }

            R.id.set_toggle -> {
                binding.puzzle.toggleSetSound()
                return true
            }

            R.id.border_toggle -> {
                binding.puzzle.toggleBorder()
                return true
            }

            R.id.win_toggle -> {
                binding.puzzle.toggleWinSound()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        common.res = requireContext().resources
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        binding.devartButton.setOnClickListener {
            hideButtons()
            val intent2 = Intent(Intent.ACTION_VIEW)
            intent2.data =
                Uri.parse(common.data.artworks[common.currentPuzzleImagePosition].urlOfArtist)
            common.blogLinksTraversed++
            startActivity(intent2)
        }

        binding.wordpressButton.setOnClickListener {
            hideButtons()
            val intent1 = Intent(Intent.ACTION_VIEW)
            intent1.data = Uri.parse(context?.getString(R.string.wordpress_link))
            common.blogLinksTraversed++
            context?.startActivity(intent1)
        }

        binding.nextButton.setOnClickListener {
            hideButtons()
            common.isImageLoaded = false
            binding.puzzle.puzzle.getNewImageLoadedScaledDivided()

            val animation = AnimationUtils.loadAnimation(context, R.anim.fade_out)
            val animationListener: Animation.AnimationListener =
                object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        binding.scoreText.visibility = View.INVISIBLE
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                }
            animation.setAnimationListener(animationListener)
            binding.scoreText.animation = animation
        }

        binding.saveImageButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val coroutineScope = CoroutineScope(Dispatchers.Main)
                coroutineScope.launch {
                    val result = withContext(Dispatchers.IO) {
                        permissionCheckSaveImage();
                    }
                    if (result) showToast("Success!") else showToast("Error!")
                }
            } else {
                saveImage();
            }
        }

        binding.saveMusicButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val coroutineScope = CoroutineScope(Dispatchers.Main)
                coroutineScope.launch {
                    val result = withContext(Dispatchers.IO) {
                        permissionCheckSaveMusic();
                    }
                    if (result) showToast("Success!") else showToast("Error!")
                }
            } else {
                saveMusic();
            }
        }

        binding.puzzle.fragment = this;
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun permissionCheckSaveImage(): Boolean = withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null
        var out: OutputStream? = null
        try {
            var cachePath = ""
            val path = File(requireContext().externalCacheDir, "music")
            path.mkdirs() // don't forget to make the directory
            val file = File(path, "\$fileName")
            val stream = FileOutputStream(file)
            val inputStream1: InputStream =
                requireContext().resources.openRawResource(Data.TRACK_01)
            val data = ByteArray(inputStream1.available())
            inputStream1.read(data)
            stream.write(data)
            inputStream1.close()
            stream.close()
            cachePath = file.absolutePath
            val cachedImgFile = File(cachePath)
            val resolver = requireContext().contentResolver
            val cachedImgUrl = cachedImgFile.toURI().toURL()
            val name = "track03"
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS
            )
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                inputStream = cachedImgUrl.openStream()
                out = resolver.openOutputStream(uri)
                inputStream.transferTo(out)
                inputStream.close()
                assert(out != null)
                out!!.close()
            }
            cachedImgFile.delete()
            common.musicSaved++
            return@withContext true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) { /* handle */
            }
            try {
                out?.close()
            } catch (e: IOException) { /*handle */
            }

        }
        return@withContext false
    }

    fun saveImage() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestReadWritePermission(MainActivity.WRITE_EXTERNAL_STORAGE_IMAGE)
        } else {
            SavePhoto(context, common.currentPuzzleImagePosition)
        }
    }

    private fun requestReadWritePermission(returnCode: Int) {
        val activity = context as Activity?
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity!!,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            Snackbar.make(
                binding.coordinatorLayout, "Allow saving with permission.",
                Snackbar.LENGTH_INDEFINITE
            ).setAction("OK",
                View.OnClickListener { view: View? ->
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf<String>(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        returnCode
                    )
                }).show()
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf<String>(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                returnCode
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun permissionCheckSaveMusic(): Boolean = withContext(Dispatchers.Default) {
        var inputStream: InputStream? = null
        var out: OutputStream? = null
        try {
            var cachePath = ""
            val path: File = File(context?.externalCacheDir, "music")
            path.mkdirs() // don't forget to make the directory
            val file = File(path, "\$fileName")
            val stream = FileOutputStream(file)
            val `is` = context?.resources?.openRawResource(Data.TRACK_01)
            val data = ByteArray(`is`?.available()!!)
            `is`.read(data)
            stream.write(data)
            `is`.close()
            stream.close()
            cachePath = file.absolutePath
            val cachedImgFile = File(cachePath)
            val resolver: ContentResolver = context?.contentResolver!!
            val cachedImgUrl = cachedImgFile.toURI().toURL()
            val name = "track03"
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS
            )
            val uri =
                resolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )
            if (uri != null) {
                inputStream = cachedImgUrl.openStream()
                out = resolver.openOutputStream(uri)
                inputStream.transferTo(out)
                inputStream.close()
                assert(out != null)
                out!!.close()
            }
            cachedImgFile.delete()
            CommonVariables.getInstance().musicSaved++
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) { /* handle */
            }
            try {
                out?.close()
            } catch (e: IOException) { /*handle */
            }
        }
        return@withContext false
    }

    private lateinit var toast: Toast
    private fun showToast(message: String) {
        // Create and show toast for save photo
        toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun startStatsFragment() {
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
    }

    public fun saveMusic() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestReadWritePermission(MainActivity.WRITE_EXTERNAL_STORAGE_MUSIC)
        } else {
            val intent = Intent(context, SaveMusicService::class.java)
            context?.startService(intent)
        }
    }

    private fun hideButtons() {
        if (binding.scoreText.visibility == View.VISIBLE) {
            binding.scoreText.visibility = View.INVISIBLE
        }
        if (binding.nextButton.visibility == View.VISIBLE) {
            binding.nextButton.visibility = View.INVISIBLE
        }
        if (binding.devartButton.visibility == View.VISIBLE) {
            binding.devartButton.visibility = View.INVISIBLE
        }
        if (binding.wordpressButton.visibility == View.VISIBLE) {
            binding.wordpressButton.visibility = View.INVISIBLE
        }
        if (binding.saveMusicButton.visibility == View.VISIBLE) {
            binding.saveMusicButton.visibility = View.INVISIBLE
        }
        if (binding.saveImageButton.visibility == View.VISIBLE) {
            binding.saveImageButton.visibility = View.INVISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
        getSharedPrefs()
    }

    override fun onResume() {
        super.onResume()
    }

    /**
     * Helper method to get total of all ints from 0 to n
     *
     * @param n
     * @return
     */
    private fun sumToPositiveN(n: Int): Int {
        return if (n <= 0) 0 else sumToPositiveN(n - 1) + n
    }

    /**
     * Get the previous puzzle and stats.
     */
    private fun getSharedPrefs() {
        if (common.isLogging) Log.d(PUZZLE_LOG, "getSharedPrefs PuzzleFragment")

        sharedpreferences = (requireContext().getSharedPreferences(
            getString(R.string.MY_PREFERENCES), Context.MODE_PRIVATE
        ))

        // check for all to be loaded here
        var isValid = false
        var posImage = 0
        if (sharedpreferences.contains(getString(R.string.IMAGENUMBER))) {
            posImage = sharedpreferences.getInt(
                getString(R.string.IMAGENUMBER), 0
            )
            if (posImage >= 0 && posImage < common.data.artworks.size) {
                isValid = true
            }
        } else {
            isValid = false
        }
        var currentTime = 0L
        if (isValid) {
            if (sharedpreferences.contains(getString(R.string.TIME))) {
                currentTime = sharedpreferences.getLong(getString(R.string.TIME), 0L)
                isValid = true
            } else {
                isValid = false
            }
        }

        // only continue if there is an image from the previous puzzle
        var slots: String? = ""
        if (isValid) {
            if (sharedpreferences.contains(getString(R.string.SLOTS))) {
                slots = sharedpreferences.getString(getString(R.string.SLOTS), "")
                if (slots == "" || slots!!.length < 2) {
                    isValid = false
                } else {
                    val slotArr = slots.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val expectedTotal: Int = sumToPositiveN(slotArr.size - 1)
                    var actualTotal = 0
                    for (aSlotArr in slotArr) {
                        try {
                            val temp = aSlotArr.toInt()
                            actualTotal += temp
                        } catch (nfe: NumberFormatException) {
                            isValid = false
                        }
                    }
                    common.dimensions = Math.sqrt(slotArr.size.toDouble())
                    if (expectedTotal != actualTotal) {
                        isValid = false
                    }
                }
            } else {
                isValid = false
            }
        }
        var playTap = false
        if (isValid) {
            if (sharedpreferences.contains(getString(R.string.SOUND))) {
                playTap = sharedpreferences.getBoolean(getString(R.string.SOUND), true)
                isValid = true
            } else {
                isValid = false
            }
        }
        var playChime = false
        if (isValid) {
            if (sharedpreferences.contains(getString(R.string.CHIME))) {
                playChime = sharedpreferences.getBoolean(
                    getString(R.string.CHIME),
                    true
                )
                isValid = true
            } else {
                isValid = false
            }
        }
        var playMusic = false
        if (isValid) {
            if (sharedpreferences.contains(getString(R.string.MUSIC))) {
                playMusic = sharedpreferences.getBoolean(
                    getString(R.string.MUSIC),
                    true
                )
                isValid = true
            } else {
                isValid = false
            }
        }
        var drawBorders = false
        if (isValid) {
            if (sharedpreferences.contains(getString(R.string.BORDER))) {
                drawBorders = sharedpreferences.getBoolean(
                    getString(R.string.BORDER),
                    true
                )
                isValid = true
            } else {
                isValid = false
            }
        }
        var posSound = 0
        if (isValid) {
            if (sharedpreferences.contains(getString(R.string.POSITION))) {
                posSound = sharedpreferences.getInt(getString(R.string.POSITION), 0)
                isValid = true
            } else {
                isValid = false
            }
        }
        if (isValid) {
            common.currentPuzzleImagePosition = posImage
            common.currentSoundPosition = posSound
            common.drawBorders = drawBorders
            common.playMusic = playMusic
            common.playChimeSound = playChime
            common.playTapSound = playTap
            common.setSlots(slots)
            common.resumePreviousPuzzle = true
            common.currPuzzleTime = currentTime
        } else {
            common.createNewPuzzle = true
        }

        //start of saved stats
        if (sharedpreferences.contains(getString(R.string.PUZZLES_SOLVED))) {
            common.puzzlesSolved = sharedpreferences.getInt(getString(R.string.PUZZLES_SOLVED), 0)
        }
        if (sharedpreferences.contains(getString(R.string.TWO_SOLVE_COUNT))) {
            common.fourPiecePuzzleSolvedCount =
                sharedpreferences.getInt(getString(R.string.TWO_SOLVE_COUNT), 0)
        }
        if (sharedpreferences.contains(getString(R.string.TWO_SOLVE_TIME))) {
            common.fourRecordSolveTime =
                sharedpreferences.getLong(getString(R.string.TWO_SOLVE_TIME), 0)
        }
        if (sharedpreferences.contains(getString(R.string.THREE_SOLVE_COUNT))) {
            common.ninePiecePuzzleSolvedCount =
                sharedpreferences.getInt(getString(R.string.THREE_SOLVE_COUNT), 0)
        }
        if (sharedpreferences.contains(getString(R.string.THREE_SOLVE_TIME))) {
            common.nineRecordSolveTime =
                sharedpreferences.getLong(getString(R.string.THREE_SOLVE_TIME), 0)
        }
        if (sharedpreferences.contains(getString(R.string.FOUR_SOLVE_COUNT))) {
            common.sixteenPiecePuzzleSolvedCount =
                sharedpreferences.getInt(getString(R.string.FOUR_SOLVE_COUNT), 0)
        }
        if (sharedpreferences.contains(getString(R.string.FOUR_SOLVE_TIME))) {
            common.sixteenRecordSolveTime =
                sharedpreferences.getLong(getString(R.string.FOUR_SOLVE_TIME), 0)
        }
        if (sharedpreferences.contains(getString(R.string.FIVE_SOLVE_COUNT))) {
            common.twentyfivePiecePuzzleSolvedCount =
                sharedpreferences.getInt(getString(R.string.FIVE_SOLVE_COUNT), 0)
        }
        if (sharedpreferences.contains(getString(R.string.FIVE_SOLVE_TIME))) {
            common.twentyfiveRecordSolveTime =
                sharedpreferences.getLong(getString(R.string.FIVE_SOLVE_TIME), 0)
        }
        if (sharedpreferences.contains(getString(R.string.SIX_SOLVE_COUNT))) {
            common.thirtysixPiecePuzzleSolvedCount =
                sharedpreferences.getInt(getString(R.string.SIX_SOLVE_COUNT), 0)
        }
        if (sharedpreferences.contains(getString(R.string.SIX_SOLVE_TIME))) {
            common.thirtysixRecordsSolveTime =
                sharedpreferences.getLong(getString(R.string.SIX_SOLVE_TIME), 0)
        }
        if (sharedpreferences.contains(getString(R.string.SEVEN_SOLVE_COUNT))) {
            common.fourtyninePiecePuzzleSolvedCount =
                sharedpreferences.getInt(getString(R.string.SEVEN_SOLVE_COUNT), 0)
        }
        if (sharedpreferences.contains(getString(R.string.SEVEN_SOLVE_TIME))) {
            common.fourtynineRecordsSolveTime =
                sharedpreferences.getLong(getString(R.string.SEVEN_SOLVE_TIME), 0)
        }
        if (sharedpreferences.contains(getString(R.string.IMAGES_SAVED))) {
            common.imagesSaved = sharedpreferences.getInt(getString(R.string.IMAGES_SAVED), 0)
        }
        if (sharedpreferences.contains(getString(R.string.BLOG_LINKS_TRAVERSED))) {
            common.blogLinksTraversed =
                sharedpreferences.getInt(getString(R.string.BLOG_LINKS_TRAVERSED), 0)
        }
        if (sharedpreferences.contains(getString(R.string.MUSIC_SAVED))) {
            common.musicSaved = sharedpreferences.getInt(getString(R.string.MUSIC_SAVED), 0)
        }
    }


    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        // myMediaPlayer.onStop()

        val slotString = binding.puzzle.slotString
        val dateLong = common.currPuzzleTime

        val editor = sharedpreferences.edit()
        editor.putInt(getString(R.string.IMAGENUMBER), common.currentPuzzleImagePosition)
        editor.putString(getString(R.string.SLOTS), slotString)
        editor.putBoolean(getString(R.string.SOUND), common.playTapSound)
        editor.putBoolean(getString(R.string.MUSIC), common.playMusic)
        editor.putBoolean(getString(R.string.CHIME), common.playChimeSound)
        editor.putBoolean(getString(R.string.BORDER), common.drawBorders)
        editor.putInt(getString(R.string.POSITION), common.currentSoundPosition)
        editor.putInt(getString(R.string.MUSIC_SAVED), common.musicSaved)
        editor.putLong(getString(R.string.TIME), dateLong)
        editor.putInt(getString(R.string.PUZZLES_SOLVED), common.puzzlesSolved)
        editor.putInt(getString(R.string.IMAGES_SAVED), common.imagesSaved)
        editor.putInt(getString(R.string.BLOG_LINKS_TRAVERSED), common.blogLinksTraversed)
        editor.putInt(getString(R.string.TWO_SOLVE_COUNT), common.fourPiecePuzzleSolvedCount)
        editor.putLong(getString(R.string.TWO_SOLVE_TIME), common.fourRecordSolveTime)
        editor.putInt(getString(R.string.THREE_SOLVE_COUNT), common.ninePiecePuzzleSolvedCount)
        editor.putLong(getString(R.string.THREE_SOLVE_TIME), common.nineRecordSolveTime)
        editor.putInt(getString(R.string.FOUR_SOLVE_COUNT), common.sixteenPiecePuzzleSolvedCount)
        editor.putLong(getString(R.string.FOUR_SOLVE_TIME), common.sixteenRecordSolveTime)
        editor.putInt(getString(R.string.FIVE_SOLVE_COUNT), common.twentyfivePiecePuzzleSolvedCount)
        editor.putLong(getString(R.string.FIVE_SOLVE_TIME), common.twentyfiveRecordSolveTime)
        editor.putInt(getString(R.string.SIX_SOLVE_COUNT), common.thirtysixPiecePuzzleSolvedCount)
        editor.putLong(getString(R.string.SIX_SOLVE_TIME), common.thirtysixRecordsSolveTime)
        editor.putInt(
            getString(R.string.SEVEN_SOLVE_COUNT),
            common.fourtyninePiecePuzzleSolvedCount
        )
        editor.putLong(getString(R.string.SEVEN_SOLVE_TIME), common.fourtynineRecordsSolveTime)
        editor.apply()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    public fun toggleUIOverlay() {
        if (common.isLogging)
            Log.d(TAG, "toggleUIOverlay CommonVariables")

        if (binding.nextButton.visibility == View.VISIBLE)
            binding.nextButton.visibility = View.INVISIBLE
        else {
            binding.nextButton.visibility = View.VISIBLE
            binding.nextButton.bringToFront()
        }

        if (binding.devartButton.visibility == View.VISIBLE)
            binding.devartButton.visibility = View.INVISIBLE
        else {
            binding.devartButton.visibility = View.VISIBLE
            binding.devartButton.bringToFront()
        }

        if (binding.wordpressButton
                .visibility == View.VISIBLE
        )
            binding.wordpressButton.visibility = View.INVISIBLE
        else {
            binding.wordpressButton.visibility = View.VISIBLE
            binding.wordpressButton.bringToFront()
        }

        if (binding.saveImageButton
                .visibility == View.VISIBLE
        )
            binding.saveImageButton.visibility = View.INVISIBLE
        else {
            binding.saveImageButton.visibility = View.VISIBLE
            binding.saveImageButton.bringToFront()
        }

        if (binding.saveMusicButton
                .visibility == View.VISIBLE
        )
            binding.saveMusicButton.visibility = View.INVISIBLE
        else {
            binding.saveMusicButton.visibility = View.VISIBLE
            binding.saveMusicButton.bringToFront()
        }

        if (binding.scoreText
                .visibility == View.VISIBLE
        )
            binding.scoreText.visibility = View.INVISIBLE
        else {
            binding.scoreText.visibility = View.VISIBLE
            binding.scoreText.bringToFront()
        }
    }

    /**
     * Run a thread on the UI to show the UI views.
     */
    private fun showButtons() {
        if (common.isLogging) Log.d(TAG, "showButtons CommonVariables")
        if (binding.nextButton.visibility == View.INVISIBLE) {
            binding.nextButton.visibility = View.VISIBLE
            binding.nextButton.bringToFront()
        }
        if (binding.devartButton.visibility == View.INVISIBLE) {
            binding.devartButton.visibility = View.VISIBLE
            binding.devartButton.bringToFront()
        }
        if (binding.wordpressButton.visibility == View.INVISIBLE) {
            binding.wordpressButton.visibility = View.VISIBLE
            binding.wordpressButton.bringToFront()
        }
        if (binding.saveImageButton.visibility == View.INVISIBLE) {
            binding.saveImageButton.visibility = View.VISIBLE
            binding.saveImageButton.bringToFront()
        }
        if (binding.saveMusicButton.visibility == View.INVISIBLE) {
            binding.saveMusicButton.visibility = View.VISIBLE
            binding.saveMusicButton.bringToFront()
        }
    }

    fun updatePhysics() {
        if (common.isLogging) Log.d(TAG, "updatePhysics PuzzleSurface")

        if (common.isPuzzleSolved) {
            val solveTime = "Solve time = " + binding.puzzle.puzzle.solveTime + " secs."
            val coroutineScope = CoroutineScope(Dispatchers.Main)
            coroutineScope.launch {
                binding.scoreText.text = solveTime
                binding.scoreText.visibility = View.VISIBLE
                val animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {}
                    override fun onAnimationRepeat(animation: Animation) {}
                })
                binding.scoreText.animation = animation
                showButtons()
            }
        }
    }
}