"""
Mechanical first pass for porting 1.7.10 NTM source files: renames classes/imports that have a direct
modern equivalent. Everything else (method names, logic that changed) still has to be fixed by hand,
the compiler errors afterwards are the to-do list.

Usage: python tools/translate.py <file or directory>...
"""
import os
import re
import sys

# (regex, replacement), applied in order
RULES = [
    (r"\r$", ""),
    # chat / text
    (r"net\.minecraft\.util\.EnumChatFormatting", "net.minecraft.ChatFormatting"),
    (r"\bEnumChatFormatting\b", "ChatFormatting"),
    # world
    (r"import net\.minecraft\.world\.World;", "import net.minecraft.world.level.Level;"),
    (r"\bWorld\b(?=\s+\w+\s*[,)=;])", "Level"),
    (r"\.worldObj\b", ".level"),
    (r"\bworldObj\b", "level"),
    (r"import net\.minecraft\.world\.IBlockAccess;", "import net.minecraft.world.level.BlockGetter;"),
    (r"\bIBlockAccess\b", "BlockGetter"),
    # tile entities
    (r"import net\.minecraft\.tileentity\.TileEntity;", "import net.minecraft.world.level.block.entity.BlockEntity;"),
    (r"\bTileEntity\b", "BlockEntity"),
    # entities
    (r"import net\.minecraft\.entity\.EntityLivingBase;", "import net.minecraft.world.entity.LivingEntity;"),
    (r"\bEntityLivingBase\b", "LivingEntity"),
    (r"import net\.minecraft\.entity\.Entity;", "import net.minecraft.world.entity.Entity;"),
    (r"import net\.minecraft\.entity\.player\.EntityPlayer;", "import net.minecraft.world.entity.player.Player;"),
    (r"\bEntityPlayer\b", "Player"),
    (r"import net\.minecraft\.entity\.player\.EntityPlayerMP;", "import net.minecraft.server.level.ServerPlayer;"),
    (r"\bEntityPlayerMP\b", "ServerPlayer"),
    (r"import net\.minecraft\.entity\.item\.EntityItem;", "import net.minecraft.world.entity.item.ItemEntity;"),
    (r"\bEntityItem\b", "ItemEntity"),
    # items / nbt
    (r"import net\.minecraft\.item\.ItemStack;", "import net.minecraft.world.item.ItemStack;"),
    (r"import net\.minecraft\.item\.Item;", "import net.minecraft.world.item.Item;"),
    (r"import net\.minecraft\.nbt\.NBTTagCompound;", "import net.minecraft.nbt.CompoundTag;"),
    (r"\bNBTTagCompound\b", "CompoundTag"),
    (r"import net\.minecraft\.nbt\.NBTTagList;", "import net.minecraft.nbt.ListTag;"),
    (r"\bNBTTagList\b", "ListTag"),
    (r"\.setInteger\(", ".putInt("),
    (r"\.getInteger\(", ".getInt("),
    (r"\.setFloat\(", ".putFloat("),
    (r"\.setDouble\(", ".putDouble("),
    (r"\.setLong\(", ".putLong("),
    (r"\.setBoolean\(", ".putBoolean("),
    (r"\.setString\(", ".putString("),
    (r"\.setShort\(", ".putShort("),
    (r"\.setByte\(", ".putByte("),
    (r"\.setTag\(", ".put("),
    (r"\.getCompoundTag\(", ".getCompound("),
    # potions -> mob effects
    (r"import net\.minecraft\.potion\.PotionEffect;", "import net.minecraft.world.effect.MobEffectInstance;"),
    (r"import net\.minecraft\.potion\.Potion;", "import net.minecraft.world.effect.MobEffects;"),
    (r"\bPotionEffect\b", "MobEffectInstance"),
    (r"\bHbmPotion\.(\w+)\.id\b", r"HbmPotion.\1"),
    # translations
    (r"import net\.minecraft\.util\.StatCollector;", "import com.hbm.util.i18n.I18nUtil;"),
    (r"\bStatCollector\.translateToLocal\(", "I18nUtil.resolveKey("),
    (r"\bStatCollector\.translateToLocalFormatted\(", "I18nUtil.resolveKey("),
    # vectors
    (r"import net\.minecraft\.util\.Vec3;", "import net.minecraft.world.phys.Vec3;"),
    (r"\bVec3\.createVectorHelper\(", "new Vec3("),
    # armor registry merged into ArmorUtil
    (r"com\.hbm\.util\.ArmorRegistry\.HazardClass", "com.hbm.util.ArmorUtil.HazardClass"),
    (r"import com\.hbm\.util\.ArmorRegistry;", "import com.hbm.util.ArmorUtil;"),
    # misc
    (r"import net\.minecraft\.util\.MathHelper;", "import net.minecraft.util.Mth;"),
    (r"\bMathHelper\.clamp_float\b", "Mth.clamp"),
    (r"\bMathHelper\.clamp_int\b", "Mth.clamp"),
    (r"\bMathHelper\.clamp_double\b", "Mth.clamp"),
    (r"\bMathHelper\.floor_double\b", "Mth.floor"),
    (r"\bMathHelper\b", "Mth"),
    (r"import net\.minecraftforge\.common\.util\.ForgeDirection;", "import net.minecraft.core.Direction;"),
    (r"\bForgeDirection\b", "Direction"),
    (r"import com\.hbm\.util\.fauxpointtwelve\.DirPos;", "import com.hbm.util.DirPos;"),
    (r"import com\.hbm\.util\.fauxpointtwelve\.BlockPos;", "import net.minecraft.core.BlockPos;"),
    (r"import cpw\.mods\.fml\.relauncher\.Side;\n", ""),
    (r"import cpw\.mods\.fml\.relauncher\.SideOnly;\n", ""),
    (r"[ \t]*@SideOnly\(Side\.CLIENT\)\n", ""),
    (r"@SideOnly\(Side\.CLIENT\) ", ""),
]


POTIONS = {
    "confusion": "CONFUSION", "moveSlowdown": "MOVEMENT_SLOWDOWN", "moveSpeed": "MOVEMENT_SPEED",
    "weakness": "WEAKNESS", "poison": "POISON", "wither": "WITHER", "hunger": "HUNGER",
    "blindness": "BLINDNESS", "digSlowdown": "DIG_SLOWDOWN", "digSpeed": "DIG_SPEED", "harm": "HARM",
    "heal": "HEAL", "regeneration": "REGENERATION", "damageBoost": "DAMAGE_BOOST", "jump": "JUMP",
    "resistance": "DAMAGE_RESISTANCE", "fireResistance": "FIRE_RESISTANCE", "waterBreathing": "WATER_BREATHING",
    "invisibility": "INVISIBILITY", "nightVision": "NIGHT_VISION", "healthBoost": "HEALTH_BOOST",
    "absorption": "ABSORPTION", "field_76443_y": "SATURATION",
}


def translate(text):
    for pattern, repl in RULES:
        text = re.sub(pattern, repl, text, flags=re.MULTILINE)
    # vanilla potions: Potion.poison.id / Potion.poison -> MobEffects.POISON
    text = re.sub(r"\bPotion\.(\w+)(?:\.id)?\b", lambda m: "MobEffects." + POTIONS.get(m.group(1), m.group(1)), text)
    return text


def handle(path):
    with open(path, encoding="utf-8") as f:
        src = f.read()
    out = translate(src)
    if out != src:
        with open(path, "w", encoding="utf-8", newline="\n") as f:
            f.write(out)


for arg in sys.argv[1:]:
    if os.path.isdir(arg):
        for dirpath, _, files in os.walk(arg):
            for name in files:
                if name.endswith(".java"):
                    handle(os.path.join(dirpath, name))
    else:
        handle(arg)
