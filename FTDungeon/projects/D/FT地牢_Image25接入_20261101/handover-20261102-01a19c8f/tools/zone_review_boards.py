from PIL import Image, ImageDraw, ImageOps

import intake_zones as intake
import recheck_zone_batch as batch


def main():
    for room_id in batch.EXPECTED:
        entry, review = batch.context(room_id)
        latest = intake.load(review / 'renders/latest.json')
        manifest = intake.load(intake.verify_file(latest['manifest']))
        inputs = [('ASSIGNED CONCEPT', entry['reference']), ('PRODUCER PREVIEW', entry['files']['preview.png'])]
        inputs += [(item['direction'].upper() + ' / ' + item['kind'], item) for item in manifest['images']]
        canvas = Image.new('RGB', (2400, 2000), '#101a26')
        drawing = ImageDraw.Draw(canvas)
        for index, (label, information) in enumerate(inputs):
            path = intake.verify_file(information)
            with Image.open(intake.io_path(path)) as picture:
                picture = picture.convert('RGB')
                if index >= 2:
                    picture = picture.crop((100, 100, 1500, 975))
                tile = ImageOps.contain(picture, (790, 465))
                left, top = index % 3 * 800, index // 3 * 500
                canvas.paste(tile, (left + (800 - tile.width) // 2, top + 30 + (465 - tile.height) // 2))
                drawing.text((left + 10, top + 7), room_id + ' / ' + label, fill='white')
        target = review / 'boards' / (latest['manifest']['sha256'] + '.png')
        intake.io_path(target).parent.mkdir(parents=True, exist_ok=True)
        if not intake.io_path(target).exists():
            canvas.save(intake.io_path(target))
        intake.save(review / 'boards/latest.json', {'board': intake.record(target), 'manifest': latest['manifest'], 'inputs': [item[1] for item in inputs]})
        print('BOARD', room_id, target, flush=True)


if __name__ == '__main__':
    main()
