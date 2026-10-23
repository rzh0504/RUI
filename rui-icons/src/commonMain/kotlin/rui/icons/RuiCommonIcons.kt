package rui.icons

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The common icon set: a curated Lucide subset, named for Rui's vocabulary
 * rather than the source glyph names.
 *
 * Every icon is a 24dp [androidx.compose.ui.graphics.vector.ImageVector]
 * with 2dp rounded strokes, ready for `RuiIcon` from the rui module.
 * Direction-dependent glyphs carry `autoMirror`, so they flip automatically
 * in RTL layouts. [All] exposes the catalogue used by the sample gallery.
 */
public object RuiCommonIcons {
    public val ChevronDown: ImageVector get() = lucideChevronDown
    public val ChevronUp: ImageVector get() = lucideChevronUp
    public val ChevronLeft: ImageVector get() = lucideChevronLeft
    public val ChevronRight: ImageVector get() = lucideChevronRight
    public val ArrowDown: ImageVector get() = lucideArrowDown
    public val ArrowUp: ImageVector get() = lucideArrowUp
    public val ArrowLeft: ImageVector get() = lucideArrowLeft
    public val ArrowRight: ImageVector get() = lucideArrowRight
    public val ArrowUpRight: ImageVector get() = lucideArrowUpRight
    public val Menu: ImageVector get() = lucideMenu
    public val MoreHorizontal: ImageVector get() = lucideMoreHorizontal
    public val MoreVertical: ImageVector get() = lucideMoreVertical
    public val Close: ImageVector get() = lucideClose
    public val Check: ImageVector get() = lucideCheck
    public val Plus: ImageVector get() = lucidePlus
    public val Minus: ImageVector get() = lucideMinus
    public val Search: ImageVector get() = lucideSearch
    public val Sliders: ImageVector get() = lucideSliders
    public val Filter: ImageVector get() = lucideFilter
    public val Settings: ImageVector get() = lucideSettings
    public val Home: ImageVector get() = lucideHome
    public val Grid: ImageVector get() = lucideGrid
    public val List: ImageVector get() = lucideList
    public val Component: ImageVector get() = lucideComponent
    public val Blocks: ImageVector get() = lucideBlocks
    public val PanelLeft: ImageVector get() = lucidePanelLeft
    public val Maximize: ImageVector get() = lucideMaximize
    public val Minimize: ImageVector get() = lucideMinimize
    public val Expand: ImageVector get() = lucideExpand
    public val Shrink: ImageVector get() = lucideShrink
    public val Move: ImageVector get() = lucideMove
    public val GripVertical: ImageVector get() = lucideGripVertical
    public val File: ImageVector get() = lucideFile
    public val FileText: ImageVector get() = lucideFileText
    public val Folder: ImageVector get() = lucideFolder
    public val FolderOpen: ImageVector get() = lucideFolderOpen
    public val Image: ImageVector get() = lucideImage
    public val Camera: ImageVector get() = lucideCamera
    public val Video: ImageVector get() = lucideVideo
    public val Music: ImageVector get() = lucideMusic
    public val Play: ImageVector get() = lucidePlay
    public val Pause: ImageVector get() = lucidePause
    public val SkipBack: ImageVector get() = lucideSkipBack
    public val SkipForward: ImageVector get() = lucideSkipForward
    public val FastForward: ImageVector get() = lucideFastForward
    public val Rewind: ImageVector get() = lucideRewind
    public val Volume: ImageVector get() = lucideVolume
    public val VolumeMute: ImageVector get() = lucideVolumeMute
    public val Mic: ImageVector get() = lucideMic
    public val MicOff: ImageVector get() = lucideMicOff
    public val Headphones: ImageVector get() = lucideHeadphones
    public val BookOpen: ImageVector get() = lucideBookOpen
    public val Bookmark: ImageVector get() = lucideBookmark
    public val Star: ImageVector get() = lucideStar
    public val Heart: ImageVector get() = lucideHeart
    public val Flag: ImageVector get() = lucideFlag
    public val Eye: ImageVector get() = lucideEye
    public val EyeOff: ImageVector get() = lucideEyeOff
    public val Link: ImageVector get() = lucideLink
    public val ExternalLink: ImageVector get() = lucideExternalLink
    public val Copy: ImageVector get() = lucideCopy
    public val Clipboard: ImageVector get() = lucideClipboard
    public val Scissors: ImageVector get() = lucideScissors
    public val Edit: ImageVector get() = lucideEdit
    public val Delete: ImageVector get() = lucideDelete
    public val Save: ImageVector get() = lucideSave
    public val Download: ImageVector get() = lucideDownload
    public val Upload: ImageVector get() = lucideUpload
    public val Share: ImageVector get() = lucideShare
    public val Printer: ImageVector get() = lucidePrinter
    public val Archive: ImageVector get() = lucideArchive
    public val Inbox: ImageVector get() = lucideInbox
    public val Pin: ImageVector get() = lucidePin
    public val Refresh: ImageVector get() = lucideRefresh
    public val Undo: ImageVector get() = lucideUndo
    public val Redo: ImageVector get() = lucideRedo
    public val Lock: ImageVector get() = lucideLock
    public val Unlock: ImageVector get() = lucideUnlock
    public val Key: ImageVector get() = lucideKey
    public val Shield: ImageVector get() = lucideShield
    public val ShieldCheck: ImageVector get() = lucideShieldCheck
    public val Wrench: ImageVector get() = lucideWrench
    public val Palette: ImageVector get() = lucidePalette
    public val Brush: ImageVector get() = lucideBrush
    public val Tag: ImageVector get() = lucideTag
    public val Gift: ImageVector get() = lucideGift
    public val ShoppingCart: ImageVector get() = lucideShoppingCart
    public val CreditCard: ImageVector get() = lucideCreditCard
    public val Wallet: ImageVector get() = lucideWallet
    public val Package: ImageVector get() = lucidePackage
    public val Truck: ImageVector get() = lucideTruck
    public val MapPin: ImageVector get() = lucideMapPin
    public val Navigation: ImageVector get() = lucideNavigation
    public val Compass: ImageVector get() = lucideCompass
    public val Globe: ImageVector get() = lucideGlobe
    public val Languages: ImageVector get() = lucideLanguages
    public val Bell: ImageVector get() = lucideBell
    public val BellOff: ImageVector get() = lucideBellOff
    public val Mail: ImageVector get() = lucideMail
    public val Phone: ImageVector get() = lucidePhone
    public val Message: ImageVector get() = lucideMessage
    public val Send: ImageVector get() = lucideSend
    public val User: ImageVector get() = lucideUser
    public val Users: ImageVector get() = lucideUsers
    public val AtSign: ImageVector get() = lucideAtSign
    public val Hash: ImageVector get() = lucideHash
    public val Clock: ImageVector get() = lucideClock
    public val Calendar: ImageVector get() = lucideCalendar
    public val Timer: ImageVector get() = lucideTimer
    public val Sun: ImageVector get() = lucideSun
    public val Moon: ImageVector get() = lucideMoon
    public val Cloud: ImageVector get() = lucideCloud
    public val CloudRain: ImageVector get() = lucideCloudRain
    public val Droplet: ImageVector get() = lucideDroplet
    public val Wifi: ImageVector get() = lucideWifi
    public val Bluetooth: ImageVector get() = lucideBluetooth
    public val Battery: ImageVector get() = lucideBattery
    public val BatteryCharging: ImageVector get() = lucideBatteryCharging
    public val Zap: ImageVector get() = lucideZap
    public val Sparkles: ImageVector get() = lucideSparkles
    public val Activity: ImageVector get() = lucideActivity
    public val CheckCircle: ImageVector get() = lucideCheckCircle
    public val XCircle: ImageVector get() = lucideXCircle
    public val AlertCircle: ImageVector get() = lucideAlertCircle
    public val Warning: ImageVector get() = lucideWarning
    public val Info: ImageVector get() = lucideInfo
    public val Help: ImageVector get() = lucideHelp
    public val Loader: ImageVector get() = lucideLoader
    public val Database: ImageVector get() = lucideDatabase
    public val Server: ImageVector get() = lucideServer
    public val Cpu: ImageVector get() = lucideCpu
    public val Code: ImageVector get() = lucideCode
    public val Terminal: ImageVector get() = lucideTerminal
    public val GitBranch: ImageVector get() = lucideGitBranch
    public val Bug: ImageVector get() = lucideBug
    public val Fingerprint: ImageVector get() = lucideFingerprint
    public val QrCode: ImageVector get() = lucideQrCode
    public val Command: ImageVector get() = lucideCommand
    public val Keyboard: ImageVector get() = lucideKeyboard
    public val Monitor: ImageVector get() = lucideMonitor
    public val Smartphone: ImageVector get() = lucideSmartphone
    public val Laptop: ImageVector get() = lucideLaptop

    /** Every curated icon in manifest order — the catalogue's gallery source. */
    public val All: List<RuiCommonIcon>
        get() = allIcons
}
