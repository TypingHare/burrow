package share

import "time"

func GetCurrentDatetimeString() string {
	return time.Now().UTC().Format("20060102_150405")
}

func GetDatetimeFromString(datetimeString string) (time.Time, error) {
	return time.Parse("20060102_150405", datetimeString)
}
