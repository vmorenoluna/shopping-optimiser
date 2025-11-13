@echo off
echo Starting Wardrobe Optimizer UI...
cd /d "%~dp0"
sbt "set fork := false" "runMain com.shoppingoptimiser.WardrobeOptimizerUI"
