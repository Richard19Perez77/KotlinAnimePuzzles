package com.compose.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.compose.myapplication.databinding.FragmentFirstBinding


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    val TAG = "com.updated.puzzles.PuzzleFragment"

    lateinit var puzzleSurface: PuzzleSurface
    // lateinit var myMediaPlayer: MyMediaPlayer
    // lateinit var mySoundPool: MySoundPool
    lateinit var sharedpreferences: SharedPreferences
    lateinit var noisyAudioStreamReceiver: NoisyAudioStreamReceiver
    var common = CommonVariables.getInstance()
    private val PUZZLE_LOG = "puzzleLog"
    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        common.devartButton = binding.devartButton
        binding.devartButton.setOnClickListener {
        }

        common.wordpressLinkButton = binding.wordpressButton
        binding.wordpressButton.setOnClickListener {
        }

        common.mNextButton = binding.nextButton
        binding.nextButton.setOnClickListener {
        }

        common.saveImageButton = binding.saveImageButton
        binding.saveImageButton.setOnClickListener {
        }

        common.saveMusicButton = binding.saveMusicButton
        binding.saveMusicButton.setOnClickListener {

        }

        common.textViewSolve = binding.scoreText

    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        getSharedPrefs();
    }

    override fun onResume() {
        super.onResume()
    }

    /**
     * Helper method to get total of all ints from 0 to n;
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
            if (posImage >= 0 || posImage < common.data.artworks.size) {
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
}