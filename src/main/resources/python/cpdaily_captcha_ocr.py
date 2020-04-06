import pytesseract
from PIL import Image
from io import BytesIO


# 图像预处理
def image_processing(image_raw):
    image = Image.open(BytesIO(image_raw))
    threshold = 127
    # 图像灰度化
    image = image.convert('L')
    # image.show()
    # 二值化处理
    table = []
    for i in range(256):
        if i < threshold:
            table.append(0)
        else:
            table.append(1)
    image = image.point(table, "1")
    return image


# 获得验证码识别结果，image_raw:图像二进制数据
def get_code(image_raw):
    image = image_processing(image_raw)
    result = pytesseract.image_to_string(image)
    return result.replace(' ', '')
